package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GfsCheckout {

  private static final int HEAD = 0;
  private static final int TARGET = 1;
  private static final int WORKTREE = 2;

  private final GitFileSystem gfs;
  private final GfsStatusProvider status;
  private final ObjectReader reader;
  private final GfsCheckoutChanges changes;

  public GfsCheckout(@Nonnull GitFileSystem gfs, boolean failOnConflict) {
    this.gfs = gfs;
    this.status = gfs.getStatusProvider();
    this.reader = gfs.getRepository().newObjectReader();
    changes = new GfsCheckoutChanges(failOnConflict);
  }

  public GfsCheckout(@Nonnull GitFileSystem gfs) {
    this(gfs, true);
  }

  public boolean checkout(@Nonnull AnyObjectId tree) throws IOException {
    TreeWalk tw = prepareTreeWalk(tree);
    mergeTreeWalk(tw);
    changes.applyTo(gfs);
    return !changes.hasConflicts();
  }

  @Nonnull
  public Map<String, GfsCheckoutConflict> getConflicts() {
    return changes.getConflicts();
  }

  @Nonnull
  private TreeWalk prepareTreeWalk(@Nonnull AnyObjectId tree) throws IOException {
    TreeWalk ret = new NameConflictTreeWalk(gfs.getRepository());
    ret.addTree(new CanonicalTreeParser(null, reader, status.commit().getTree()));
    ret.addTree(tree);
    ret.addTree(new GfsTreeIterator(gfs));
    return ret;
  }

  private void mergeTreeWalk(@Nonnull TreeWalk tw) throws IOException {
    while(tw.next())
      if(mergeEntry(tw))
        tw.enterSubtree();
  }

  private boolean mergeEntry(@Nonnull TreeWalk tw) throws IOException {
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
    changes.addConflict(new GfsCheckoutConflict(tw.getPathString(), tw.getNameString(), tw.getDepth(), head, target, worktree));
    return false;
  }

}
