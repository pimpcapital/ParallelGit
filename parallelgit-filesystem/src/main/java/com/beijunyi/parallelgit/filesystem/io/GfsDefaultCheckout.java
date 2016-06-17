package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.*;

import static com.beijunyi.parallelgit.filesystem.io.GfsCheckoutConflict.threeWayConflict;
import static com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator.iterateRoot;
import static com.beijunyi.parallelgit.filesystem.utils.GfsPathUtils.toAbsolutePath;

public class GfsDefaultCheckout {

  private static final int HEAD = 0;
  private static final int TARGET = 1;
  private static final int WORKTREE = 2;

  private final GitFileSystem gfs;
  private final GfsStatusProvider status;
  private final ObjectReader reader;
  protected final GfsCheckoutChangesCollector changes;

  private Set<String> ignoredFiles;

  public GfsDefaultCheckout(GitFileSystem gfs, boolean failOnConflict) {
    this.gfs = gfs;
    this.status = gfs.getStatusProvider();
    this.reader = gfs.getRepository().newObjectReader();
    changes = new GfsCheckoutChangesCollector(failOnConflict);
  }

  public GfsDefaultCheckout(GitFileSystem gfs) {
    this(gfs, true);
  }

  public static void checkout(GitFileSystem gfs, AnyObjectId tree) throws IOException {
    new GfsDefaultCheckout(gfs).checkout(tree);
  }

  @Nonnull
  public GfsDefaultCheckout ignoredFiles(Collection<String> ignoredFiles) {
    this.ignoredFiles = new HashSet<>(ignoredFiles);
    return this;
  }

  public void checkout(AbstractTreeIterator iterator) throws IOException {
    TreeWalk tw = prepareTreeWalk(iterator);
    collectChanges(tw);
    if(!hasConflicts())
      applyChanges();
  }

  public void checkout(AnyObjectId tree) throws IOException {
    checkout(new CanonicalTreeParser(null, reader, tree));
  }

  public void checkout(DirCache cache) throws IOException {
    checkout(new DirCacheIterator(cache));
  }

  public boolean hasConflicts() {
    return changes.hasConflicts();
  }

  @Nonnull
  public Map<String, GfsCheckoutConflict> getConflicts() {
    return changes.getConflicts();
  }

  protected boolean skips(String path) {
    return ignoredFiles != null && ignoredFiles.contains(path);
  }

  protected void applyChanges() throws IOException {
    if(!changes.isEmpty())
      changes.applyTo(gfs);
  }

  @Nonnull
  private TreeWalk prepareTreeWalk(AbstractTreeIterator iterator) throws IOException {
    TreeWalk ret = new NameConflictTreeWalk(gfs.getRepository());
    ret.addTree(new CanonicalTreeParser(null, reader, status.commit().getTree()));
    ret.addTree(iterator);
    ret.addTree(iterateRoot(gfs));
    return ret;
  }

  private void collectChanges(TreeWalk tw) throws IOException {
    while(tw.next()) {
      String path = toAbsolutePath(tw.getPathString());
      if(skips(path))
        continue;
      GitFileEntry head = GitFileEntry.newEntry(tw, HEAD);
      GitFileEntry target = GitFileEntry.newEntry(tw, TARGET);
      GitFileEntry worktree = GitFileEntry.newEntry(tw, WORKTREE);
      if(mergeEntries(path, head, target, worktree)) tw.enterSubtree();
    }
  }

  private boolean mergeEntries(String path, GitFileEntry head, GitFileEntry target, GitFileEntry worktree) throws IOException {
    if(target.equals(worktree) || target.equals(head)) return false;
    if(head.equals(worktree)) {
      changes.addChange(path, target);
      return target.isVirtualSubtree();
    }
    if(target.isSubtree() && worktree.isSubtree()) return true;
    changes.addConflict(threeWayConflict(path, head, target, worktree));
    return false;
  }

}
