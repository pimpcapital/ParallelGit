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
  private final GfsChangeCollector collector;

  public GfsCheckout(@Nonnull GitFileSystem gfs, boolean failOnConflict) {
    this.gfs = gfs;
    this.status = gfs.getStatusProvider();
    this.reader = gfs.getRepository().newObjectReader();
    collector = new GfsChangeCollector(failOnConflict);
  }

  public GfsCheckout(@Nonnull GitFileSystem gfs) {
    this(gfs, true);
  }

  public boolean checkout(@Nonnull AnyObjectId tree) throws IOException {
    TreeWalk tw = prepareTreeWalk(tree);
    GfsChangeCollector changes = parseEntries(tw);
    changes.applyTo(gfs);
    return !collector.hasConflicts();
  }

  @Nonnull
  public Map<String, GfsCheckoutConflict> getConflicts() {
    return collector.getConflicts();
  }

  @Nonnull
  private TreeWalk prepareTreeWalk(@Nonnull AnyObjectId tree) throws IOException {
    TreeWalk ret = new NameConflictTreeWalk(gfs.getRepository());
    ret.addTree(new CanonicalTreeParser(null, reader, status.commit().getTree()));
    ret.addTree(tree);
    ret.addTree(new GfsTreeIterator(gfs));
    return ret;
  }

  @Nonnull
  private GfsChangeCollector parseEntries(@Nonnull TreeWalk tw) throws IOException {
    while(tw.next())
      if(parseEntry(tw, collector) && tw.isSubtree())
        tw.enterSubtree();
    return collector;
  }

  private boolean parseEntry(@Nonnull TreeWalk tw, @Nonnull GfsChangeCollector collector) throws IOException {
    GitFileEntry head = GitFileEntry.forTreeNode(tw, HEAD);
    GitFileEntry target = GitFileEntry.forTreeNode(tw, TARGET);
    GitFileEntry worktree = GitFileEntry.forTreeNode(tw, WORKTREE);
    if(target.equals(worktree) || target.equals(head))
      return false;
    if(head.equals(worktree)) {
      collector.addChange(tw.getPathString(), target);
      return false;
    }
    if(target.isDirectory() && worktree.isDirectory())
      return true;
    collector.addConflict(new GfsCheckoutConflict(tw.getPathString(), tw.getNameString(), tw.getDepth(), head, target, worktree));
    return false;
  }

}
