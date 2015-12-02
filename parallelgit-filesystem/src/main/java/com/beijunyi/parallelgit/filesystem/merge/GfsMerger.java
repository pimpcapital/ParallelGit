package com.beijunyi.parallelgit.filesystem.merge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.MergeNotStartedException;
import com.beijunyi.parallelgit.filesystem.io.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.io.GfsIO;
import com.beijunyi.parallelgit.filesystem.io.Node;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.*;
import org.eclipse.jgit.revwalk.RevTree;

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

  private GfsMergeResult result;

  public GfsMerger(@Nonnull GitFileSystem gfs) {
    super(gfs.getRepository());
    this.gfs = gfs;
    algorithm = defaultAlgorithm(db);
    formatter = defaultFormatter();
    conflictMarkers = defaultConflictMarkers();
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

  @Nonnull
  public GitFileSystem getFileSystem() {
    return gfs;
  }

  @Nonnull
  public Map<String, GfsMergeConflict> getConflicts() {
    return conflicts;
  }

  @Nonnull
  @Override
  public ObjectId getResultTreeId() {
    if(result == null)
      throw new MergeNotStartedException();
    return (ObjectId) result.getTreeId();
  }

  @Override
  protected boolean mergeImpl() throws IOException {
    ThreeWayWalker walker = prepareWalker();
    ContentMergeConfig cmConfig = new ContentMergeConfig(algorithm, formatter, conflictMarkers);
    GfsRecursiveMerger merger = new GfsRecursiveMerger(walker, cmConfig);
    if(merger.merge())
      result =

    return result.isSuccessful();
  }

  @Nonnull
  private ThreeWayWalker prepareWalker() throws IOException {
    DirectoryNode root = gfs.getFileStore().getRoot();
    RevTree ourTree = sourceTrees[0];
    RevTree theirTree = sourceTrees[1];
    ThreeWayWalkerConfig config = new ThreeWayWalkerConfig(root, mergeBase(), ourTree, theirTree);
    return new ThreeWayWalker(config, reader);
  }

  private boolean mergeTreeNode() throws IOException {
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
    if(mergedMode != null)
      updateNode(ourId, mergedMode);
    else
      addConflict();
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
    FileMode mode = mergedMode == null ? REGULAR_FILE : mergedMode;

    byte[] bytes;
    try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      formatter.formatMerge(out, result, conflictMarkers, CHARACTER_ENCODING);
      bytes = out.toByteArray();
    }

    if(!result.containsConflicts())
      GfsIO.addChildFile(name, bytes, mode, currentDirectory, gfs);
    else
      updateNode(gfs.saveBlob(bytes), mode);
  }

  @Nonnull
  private RawText getRawText(@Nonnull AnyObjectId id) throws IOException {
    if(id.equals(ObjectId.zeroId()))
      return new RawText(new byte[0]);
    return new RawText(reader.open(id, OBJ_BLOB).getCachedBytes());
  }

  private void updateNode(@Nonnull AnyObjectId id, @Nonnull FileMode mode) throws IOException {
    if(!mode.equals(MISSING)) {
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

  @Nonnull
  private static MergeAlgorithm defaultAlgorithm(@Nonnull Repository repo) {
    SupportedAlgorithm diffAlg = repo.getConfig().getEnum(CONFIG_DIFF_SECTION, null, CONFIG_KEY_ALGORITHM, HISTOGRAM);
    return new MergeAlgorithm(DiffAlgorithm.getAlgorithm(diffAlg));
  }

  @Nonnull
  private static MergeFormatter defaultFormatter() {
    return new MergeFormatter();
  }

  @Nonnull
  private static List<String> defaultConflictMarkers() {
    return Arrays.asList("BASE", "OURS", "THEIRS");
  }

}
