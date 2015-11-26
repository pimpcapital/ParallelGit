package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import java.util.LinkedList;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.DirectoryNode;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

public class ThreeWayWalkerConfiguration {

  private final DirectoryNode root;
  private final AbstractTreeIterator base;
  private final RevTree ours;
  private final RevTree theirs;

  public ThreeWayWalkerConfiguration(@Nonnull DirectoryNode root, @Nonnull AbstractTreeIterator base, @Nonnull RevTree ours, @Nonnull RevTree theirs) {
    this.root = root;
    this.base = base;
    this.ours = ours;
    this.theirs = theirs;
  }

  @Nonnull
  public TreeWalk prepareTreeWalk(@Nonnull ObjectReader reader) throws IOException {
    TreeWalk ret = new NameConflictTreeWalk(reader);
    ret.addTree(base);
    ret.addTree(ours);
    ret.addTree(theirs);
    return ret;
  }

  @Nonnull
  public LinkedList<DirectoryNode> getDirectories() {
    LinkedList<DirectoryNode> ret = new LinkedList<>();
    ret.add(root);
    return ret;
  }

}
