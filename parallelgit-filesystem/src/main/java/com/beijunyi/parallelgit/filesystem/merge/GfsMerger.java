package com.beijunyi.parallelgit.filesystem.merge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.io.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.io.GfsIO;
import com.beijunyi.parallelgit.filesystem.io.Node;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.merge.*;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import static org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm.HISTOGRAM;
import static org.eclipse.jgit.lib.ConfigConstants.*;
import static org.eclipse.jgit.lib.Constants.*;
import static org.eclipse.jgit.lib.FileMode.*;

public class GfsMerger extends ThreeWayMerger {

  private final GitFileSystem gfs;
  private final Map<String, GfsMergeConflict> conflicts = new LinkedHashMap<>();

  private MergeAlgorithm algorithm;
  private MergeFormatter formatter;
  private List<String> conflictMarkers;
  private boolean formatConflicts;
  private TreeWalk tw;

  private DirectoryNode currentDirectory;
  private int currentDepth;

  private String path;
  private String name;
  private FileMode baseMode;
  private FileMode ourMode;
  private FileMode theirMode;
  private AnyObjectId baseId;
  private AnyObjectId ourId;
  private AnyObjectId theirId;

  private AnyObjectId resultTree;

  public GfsMerger(@Nonnull GitFileSystem gfs) {
    super(gfs.getRepository());
    this.gfs = gfs;
    tw = new NameConflictTreeWalk(reader);
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
    prepareTreeWalk();
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

  private void prepareTreeWalk() throws IOException {
    tw = new NameConflictTreeWalk(reader);
    tw.addTree(mergeBase());
    tw.addTree(sourceTrees[0]);
    tw.addTree(sourceTrees[1]);
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
      enterDirectory();
      return true;
    }

    handleFileDirectoryConflict();
    return false;
  }

  private void readTreeNodes() {
    path = tw.getPathString();
    name = tw.getNameString();
    baseMode = tw.getFileMode(0);
    baseId = tw.getObjectId(0);
    ourMode = tw.getFileMode(1);
    ourId = tw.getObjectId(1);
    theirMode = tw.getFileMode(2);
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

  private void applyTheirs() throws IOException {
    updateNode(theirId, theirMode);
  }

  private boolean theirsIsNotChanged() {
    return baseMode == theirMode && baseId.equals(theirId);
  }

  private void applyOurs() throws IOException {
    updateNode(ourId, ourMode);
  }

  private boolean bothHaveSameId() {
    return ourId.equals(theirId);
  }

  private void applyCommonChanges() throws IOException {
    FileMode mergedMode = mergeFileModes();
    if(!mergedMode.equals(MISSING))
      updateNode(ourId, mergedMode);
    else
      addConflict();
  }

  @Nonnull
  private FileMode mergeFileModes() {
    if (ourMode == theirMode)
      return ourMode;
    if (baseMode == ourMode)
      return theirMode.equals(MISSING) ? ourMode : theirMode;
    if (baseMode == theirMode)
      return ourMode.equals(MISSING) ? theirMode : ourMode;
    return MISSING;
  }

  private boolean bothAreBlob() {
    return !ourMode.equals(TREE) && !theirMode.equals(TREE);
  }

  private void mergeAndApplyBlob() throws IOException {
    if(ourMode.equals(GITLINK) || theirMode.equals(GITLINK))
      addConflict();
    else {
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
    FileMode mergedMode = mergeFileModes();
    FileMode mode = mergedMode.equals(MISSING) ? REGULAR_FILE : mergedMode;

    byte[] bytes;
    try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      formatter.formatMerge(out, result, conflictMarkers, CHARACTER_ENCODING);
      bytes = out.toByteArray();
    }

    if(!result.containsConflicts())
      GfsIO.addChildFile(name, bytes, mode, currentDirectory, gfs);
    else
      GfsIO.addChild(name, gfs.saveBlob(bytes), mode, currentDirectory, gfs);
  }

  @Nonnull
  private RawText getRawText(@Nonnull AnyObjectId id) throws IOException {
    if(id.equals(ObjectId.zeroId()))
      return new RawText(new byte[0]);
    return new RawText(reader.open(id, OBJ_BLOB).getCachedBytes());
  }

  private void updateNode(@Nonnull AnyObjectId id, @Nonnull FileMode mode) throws IOException {
    if(ourMode.equals(MISSING)) {
      Node node = GfsIO.getChild(name, currentDirectory, gfs);
      if(node != null)
        node.reset(id, mode);
      else
        GfsIO.addChild(name, id, mode, currentDirectory, gfs);
    } else
      GfsIO.removeChild(name, currentDirectory, gfs);
  }

  private void addConflict() {
    conflicts.put(path, new GfsMergeConflict(baseMode, baseId, ourMode, ourId, theirMode, theirId));
  }

  private boolean bothAreTree() {
    return ourMode.equals(TREE) && theirMode.equals(TREE);
  }

  private void enterDirectory() throws IOException {
    Node node = GfsIO.getChild(name, currentDirectory, gfs);
    if(node != null && !node.isDirectory()) {
      GfsIO.removeChild(name, currentDirectory, gfs);
      node = null;
    }
    if(node == null)
      node = GfsIO.addChildDirectory(name, currentDirectory, gfs);
    currentDirectory = (DirectoryNode) node;
    currentDepth++;
  }

  private void handleFileDirectoryConflict() throws IOException {
    updateNode(ourId, ourMode);
    addConflict();
  }

}
