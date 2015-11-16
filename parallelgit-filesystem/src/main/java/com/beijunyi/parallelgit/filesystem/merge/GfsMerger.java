package com.beijunyi.parallelgit.filesystem.merge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.io.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.io.Node;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.merge.*;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import static org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm.HISTOGRAM;
import static org.eclipse.jgit.lib.ConfigConstants.*;
import static org.eclipse.jgit.lib.Constants.*;

public class GfsMerger extends ThreeWayMerger {

  private final GitFileSystem gfs;
  private final Map<String, GfsMergeConflict> conflicts;

  private MergeAlgorithm algorithm;
  private MergeFormatter formatter;
  private List<String> conflictMarkers;
  private boolean formatConflicts;
  private TreeWalk tw;

  private DirectoryNode currentDirectory;
  private int currentDepth;

  private String path;
  private String name;
  private int baseMode;
  private int ourMode;
  private int theirMode;
  private AnyObjectId baseId;
  private AnyObjectId ourId;
  private AnyObjectId theirId;

  private AnyObjectId resultTree;

  public GfsMerger(@Nonnull GitFileSystem gfs) {
    super(gfs.getRepository());
    this.gfs = gfs;

    tw = new NameConflictTreeWalk(reader);
    conflicts = new HashMap<>();

    currentDirectory = gfs.getFileStore().getRoot();
    currentDepth = 0;
  }

  @Nullable
  public MergeAlgorithm getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(@Nullable MergeAlgorithm algorithm) {
    this.algorithm = algorithm;
  }

  @Nullable
  public MergeFormatter getFormatter() {
    return formatter;
  }

  public void setFormatter(@Nullable MergeFormatter formatter) {
    this.formatter = formatter;
  }

  @Nullable
  public List<String> getConflictMarkers() {
    return conflictMarkers;
  }

  public void setConflictMarkers(@Nullable List<String> conflictMarkers) {
    this.conflictMarkers = conflictMarkers;
  }

  public boolean isFormatConflicts() {
    return formatConflicts;
  }

  public void setFormatConflicts(boolean formatConflicts) {
    this.formatConflicts = formatConflicts;
  }

  @Nonnull
  public GitFileSystem getFileSystem() {
    return gfs;
  }

  @Nonnull
  public Map<String, GfsMergeConflict> getConflicts() {
    return conflicts;
  }

  @Override
  public ObjectId getResultTreeId() {
    return (ObjectId) resultTree;
  }

  @Override
  protected boolean mergeImpl() throws IOException {
    prepareAlgorithm();
    prepareFormatter();
    prepareConflictMarkers();
    prepareTreeWalk(mergeBase(), sourceTrees[0], sourceTrees[1]);
    mergeTreeWalk();
    if(conflicts.isEmpty()) {
      resultTree = gfs.persist();
      return true;
    } else
      gfs.flush();
    return false;
  }

  private void prepareAlgorithm() {
    if(algorithm == null) {
      SupportedAlgorithm diffAlg = db.getConfig().getEnum(CONFIG_DIFF_SECTION, null, CONFIG_KEY_ALGORITHM, HISTOGRAM);
      algorithm = new MergeAlgorithm(DiffAlgorithm.getAlgorithm(diffAlg));
    }
  }

  private void prepareFormatter() {
    if(formatter == null) {
      formatter = new MergeFormatter();
    }
  }

  private void prepareConflictMarkers() {
    if(conflictMarkers == null)
      conflictMarkers = Arrays.asList("BASE", "OURS", "THEIRS");
  }

  private void prepareTreeWalk(@Nonnull AbstractTreeIterator base, @Nonnull RevTree ours, @Nonnull RevTree theirs) throws IOException {
    tw = new NameConflictTreeWalk(reader);
    tw.addTree(base);
    tw.addTree(ours);
    tw.addTree(theirs);
  }

  private void mergeTreeWalk() throws IOException {
    while(tw.next()) {
      if(mergeTreeNode() && tw.isSubtree())
        tw.enterSubtree();
    }
  }

  private boolean mergeTreeNode() throws IOException {
    readTreeNodes();
    prepareDirectory();

    if(oursIsNotChanged()) {
      applyTheirs();
      return false;
    }

    if(theirsIsNotChanged()) {
      applyOurs();
      return false;
    }

    if(bothHaveSameId()) {
      applyCommonChanges();
      return false;
    }

    if(bothAreBlob()) {
      mergeAndApplyBlob();
      return false;
    }

    if(bothAreTree()) {
      addDirectory();
      return true;
    }

    handleFileDirectoryConflict();
    return false;
  }

  private void readTreeNodes() {
    path = tw.getPathString();
    name = tw.getNameString();
    baseMode = tw.getRawMode(0);
    baseId = tw.getObjectId(0);
    ourMode = tw.getRawMode(1);
    ourId = tw.getObjectId(1);
    theirMode = tw.getRawMode(2);
    theirId = tw.getObjectId(2);
  }

  private void prepareDirectory() {
    int depth = tw.getDepth();
    while(currentDepth > depth) {
      currentDirectory = currentDirectory.getParent();
      currentDepth--;
    }
  }

  private boolean oursIsNotChanged() {
    return baseMode == ourMode && baseId.equals(ourId);
  }

  private boolean applyTheirs() {
    if(theirMode != FileMode.TYPE_MISSING)
      insertNode(theirMode, theirId);
    return false;
  }

  private boolean theirsIsNotChanged() {
    return baseMode == theirMode && baseId.equals(theirId);
  }

  private void applyOurs() {
    if(ourMode != FileMode.TYPE_MISSING)
      insertNode(ourMode, ourId);
  }

  private boolean bothHaveSameId() {
    return ourId.equals(theirId);
  }

  private void applyCommonChanges() {
    int mergedMode = mergeFileModes();
    if(mergedMode != FileMode.TYPE_MISSING)
      insertNode(mergedMode, ourId);
    else {
      insertNode(ourMode, ourId);
      addConflict();
    }
  }

  private int mergeFileModes() {
    if (ourMode == theirMode)
      return ourMode;
    if (baseMode == ourMode)
      return theirMode == FileMode.TYPE_MISSING ? ourMode : theirMode;
    if (baseMode == theirMode)
      return ourMode == FileMode.TYPE_MISSING ? theirMode : ourMode;
    return FileMode.TYPE_MISSING;
  }

  private boolean bothAreBlob() {
    return ourMode != FileMode.TYPE_TREE && theirMode != FileMode.TYPE_TREE;
  }

  private void mergeAndApplyBlob() throws IOException {
    if(ourMode == FileMode.TYPE_GITLINK || theirMode == FileMode.TYPE_GITLINK) {
      insertNode(ourMode, ourId);
      addConflict();
    } else {
      MergeResult<RawText> result = mergeContent();
      writeMergedFile(result);
      if(result.containsConflicts())
        addConflict();
    }
  }

  @Nonnull
  private MergeResult<RawText> mergeContent() throws IOException {
    RawText baseContent = getRawText(baseId);
    RawText ourContent = getRawText(ourId);
    RawText theirContent = getRawText(theirId);
    return algorithm.merge(RawTextComparator.DEFAULT, baseContent, ourContent, theirContent);
  }

  private void writeMergedFile(@Nonnull MergeResult<RawText> result) throws IOException {
    int mergedMode = mergeFileModes();
    FileMode mode = mergedMode == FileMode.TYPE_MISSING ? FileMode.REGULAR_FILE : FileMode.fromBits(mergedMode);

    byte[] bytes;
    try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      formatter.formatMerge(out, result, conflictMarkers, CHARACTER_ENCODING);
      bytes = out.toByteArray();
    }

    Node node;
    if(!result.containsConflicts())
      node = Node.forBytes(bytes, mode, currentDirectory);
    else {
      AnyObjectId blob = gfs.saveBlob(bytes);
      node = Node.forObject(blob, mode, currentDirectory);
    }

    addChild(node);
  }

  @Nonnull
  private RawText getRawText(@Nonnull AnyObjectId id) throws IOException {
    if(id.equals(ObjectId.zeroId()))
      return new RawText(new byte[0]);
    return new RawText(reader.open(id, OBJ_BLOB).getCachedBytes());
  }

  private void insertNode(int mode, @Nonnull AnyObjectId id) {
    Node node = Node.forObject(id, FileMode.fromBits(mode), currentDirectory);
    addChild(node);
  }

  private void addChild(@Nonnull Node child) {
    if(currentDirectory.getChildren() == null)
      currentDirectory.setChildren(new ConcurrentHashMap<String, Node>());
    currentDirectory.addChild(name, child, false);
  }

  private void addConflict() {
    conflicts.put(path, new GfsMergeConflict(baseMode, baseId, ourMode, ourId, theirMode, theirId));
  }

  private boolean bothAreTree() {
    return ourMode == FileMode.TYPE_TREE && theirMode == FileMode.TYPE_TREE;
  }

  private void addDirectory() {
    DirectoryNode node = DirectoryNode.newDirectory(currentDirectory);
    addChild(node);
    currentDirectory = node;
  }

  private void handleFileDirectoryConflict() {
    insertNode(ourMode, ourId);
    addConflict();
  }

}
