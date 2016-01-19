package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.MergeNotStartedException;
import com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeAlgorithm;
import org.eclipse.jgit.merge.MergeFormatter;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import static org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm.HISTOGRAM;
import static org.eclipse.jgit.lib.ConfigConstants.*;

public class GfsMerge extends ThreeWayMerger {

  private static final int BASE = 0;
  private static final int HEAD = 1;
  private static final int TARGET = 2;
  private static final int WORKTREE = 3;

  private final GfsMergeChanges changes = new GfsMergeChanges();
  private final GitFileSystem gfs;
  private final GfsStatusProvider status;
  private final ObjectReader reader;

  private MergeAlgorithm algorithm = defaultAlgorithm(db);
  private MergeFormatter formatter = defaultFormatter();
  private List<String> conflictMarkers = defaultConflictMarkers();

  private GfsMergeResult result;

  public GfsMerge(@Nonnull GitFileSystem gfs) {
    super(gfs.getRepository());
    this.gfs = gfs;
    this.status = gfs.getStatusProvider();
    this.reader = gfs.getRepository().newObjectReader();
  }

  @Override
  protected boolean mergeImpl() throws IOException {
    TreeWalk tw = prepareTreeWalk();
    mergeTreeWalk(tw);
    return !changes.hasFailedPaths() && !changes.hasConflicts();
  }

  @Override
  public ObjectId getResultTreeId() {
    if(result == null)
      throw new MergeNotStartedException();
    return (ObjectId) result.getTree();
  }

  @Nonnull
  private TreeWalk prepareTreeWalk() throws IOException {
    TreeWalk ret = new NameConflictTreeWalk(gfs.getRepository());
    ret.addTree(mergeBase());
    ret.addTree(new CanonicalTreeParser(null, reader, sourceTrees[0]));
    ret.addTree(sourceTrees[1]);
    ret.addTree(new GfsTreeIterator(gfs));
    return ret;
  }

  private void mergeTreeWalk(@Nonnull TreeWalk tw) throws IOException {
    while(tw.next())
      if(mergeEntry(tw))
        tw.enterSubtree();
  }

  private boolean mergeEntry(@Nonnull TreeWalk tw) throws IOException {
    GitFileEntry base = GitFileEntry.forTreeNode(tw, BASE);
    GitFileEntry head = GitFileEntry.forTreeNode(tw, HEAD);
    GitFileEntry target = GitFileEntry.forTreeNode(tw, TARGET);
    GitFileEntry worktree = GitFileEntry.forTreeNode(tw, WORKTREE);
    if(target.equals(worktree) || target.equals(head))
      return false;
    if(head.equals(worktree)) {
      changes.addChange(tw.getPathString(), target);
      return false;
    }
    if(target.isDirectory() && worktree.isDirectory())
      return true;
    changes.addConflict(new GfsMergeConflict(tw.getPathString(), tw.getNameString(), tw.getDepth(), base, head, target, worktree));
    return false;
  }


  @Nonnull
  private static MergeAlgorithm defaultAlgorithm(@Nonnull Repository repo) {
    DiffAlgorithm.SupportedAlgorithm diffAlg = repo.getConfig().getEnum(CONFIG_DIFF_SECTION, null, CONFIG_KEY_ALGORITHM, HISTOGRAM);
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
