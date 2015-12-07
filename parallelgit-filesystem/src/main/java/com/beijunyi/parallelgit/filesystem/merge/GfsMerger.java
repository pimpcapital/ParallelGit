package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.MergeNotStartedException;
import com.beijunyi.parallelgit.filesystem.io.DirectoryNode;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeAlgorithm;
import org.eclipse.jgit.merge.MergeFormatter;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevTree;

import static org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm.HISTOGRAM;
import static org.eclipse.jgit.lib.ConfigConstants.*;

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
    return (ObjectId) result.getTree();
  }

  @Override
  protected boolean mergeImpl() throws IOException {
    ThreeWayWalker walker = prepareWalker();
    ContentMergeConfig cmConfig = new ContentMergeConfig(algorithm, formatter, conflictMarkers);
    GfsRecursiveMerger merger = new GfsRecursiveMerger(walker, cmConfig, reader);
    if(merger.merge()) {
      result = GfsMergeResult.success(gfs.flush());
      return true;
    } else {
      result = GfsMergeResult.conflicting(merger.getConflicts());
      return false;
    }
  }

  @Nonnull
  private ThreeWayWalker prepareWalker() throws IOException {
    DirectoryNode root = gfs.getFileStore().getRoot();
    RevTree ourTree = sourceTrees[0];
    RevTree theirTree = sourceTrees[1];
    ThreeWayWalkerConfig config = new ThreeWayWalkerConfig(root, mergeBase(), ourTree, theirTree);
    return new ThreeWayWalker(config, reader);
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
