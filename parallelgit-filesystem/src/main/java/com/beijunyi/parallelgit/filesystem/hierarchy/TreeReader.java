package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.TreeWalk;

public class TreeReader {

  private ObjectReader reader;
  private AnyObjectId tree;
  private TreeWalk treeWalk;
  private String base;
  private DirectoryNode parent;

  @Nonnull
  public static TreeReader prepare() {
    return new TreeReader();
  }

  @Nonnull
  public TreeReader reader(@Nonnull ObjectReader reader) {
    this.reader = reader;
    return this;
  }

  private void prepareObjectReader() {
    if(reader == null)
      throw new IllegalArgumentException("Missing object reader");
  }

  @Nonnull
  public TreeReader tree(@Nonnull AnyObjectId tree) {
    this.tree = tree;
    return this;
  }

  @Nonnull
  public TreeReader tree(@Nonnull TreeWalk treeWalk) {
    this.treeWalk = treeWalk;
    return this;
  }

  public void errorMissingTree() {
    throw new IllegalArgumentException("Missing tree");
  }

  public void errorMultipleTrees() {
    throw new IllegalArgumentException("Multiple trees specified");
  }

  public void errorDifferentTrees(@Nonnull String t1, @Nonnull String t2) {
    throw new IllegalArgumentException("Different tree found: " + t1 + ", " + t2);
  }

  private void prepareTreeWalk() throws IOException {
    if(treeWalk == null) {
      if(tree == null)
        errorMissingTree();
      treeWalk = new TreeWalk(reader);
      treeWalk.addTree(tree);
    } else if(treeWalk.getTreeCount() == 0)
      errorMissingTree();
      else if(treeWalk.getTreeCount() > 0)
      errorMultipleTrees();
      else if(treeWalk.getObjectId(0).equals(tree))
      errorDifferentTrees(treeWalk.getObjectId(0).getName(), tree.getName());
  }

  @Nonnull
  public TreeReader base(@Nonnull String base) {
    this.base = base;
    return this;
  }

  @Nonnull
  public TreeReader root() {
    return base("");
  }

  private void prepareBase() {
    if(base == null)
      throw new IllegalArgumentException("Missing base path");
  }

  @Nonnull
  public DirectoryNode doRead() throws IOException {
    DirectoryNode node = new DirectoryNode();
    while(treeWalk.next()) {

    }
    return node;
  }

  @Nonnull
  public DirectoryNode read() throws IOException {
    prepareObjectReader();
    prepareTreeWalk();
    prepareBase();
    return doRead();
  }
}
