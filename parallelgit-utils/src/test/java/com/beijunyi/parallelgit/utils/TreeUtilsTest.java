package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TreeUtilsTest extends AbstractParallelGitTest {

  private static void assertNextEntry(@Nonnull TreeWalk treeWalk, String path) throws IOException {
    Assert.assertTrue(treeWalk.next());
    Assert.assertEquals(path, treeWalk.getPathString());
  }

  @Before
  public void setUp() throws Exception {
    initRepository();
  }

  @Test
  public void newTreeWalkTest() throws IOException {
    clearCache();
    writeMultipleToCache("/a.txt", "/b.txt", "/c/d.txt", "/c/e.txt", "/f/g.txt");
    RevTree tree = commitToMaster().getTree();
    TreeWalk treeWalk = TreeUtils.newTreeWalk(tree, repo);

    assertNextEntry(treeWalk, "a.txt");
    assertNextEntry(treeWalk, "b.txt");
    assertNextEntry(treeWalk, "c");
    assertNextEntry(treeWalk, "f");
    Assert.assertFalse(treeWalk.next());
  }

  @Test
  public void existsTest() throws IOException {
    writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    Assert.assertTrue(TreeUtils.exists("a", tree, repo));
    Assert.assertTrue(TreeUtils.exists("a/b.txt", tree, repo));
    Assert.assertFalse(TreeUtils.exists("a/b", tree, repo));
  }

  @Test
  public void getObjectTest() throws IOException {
    AnyObjectId objectId = writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    Assert.assertEquals(objectId, TreeUtils.getObjectId("a/b.txt", tree, repo));
  }

  @Test
  public void isBlobTest() throws IOException {
    writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isFileOrSymbolicLink("a", tree, repo));
    Assert.assertTrue(TreeUtils.isFileOrSymbolicLink("a/b.txt", tree, repo));
    Assert.assertFalse(TreeUtils.isFileOrSymbolicLink("a/b", tree, repo));
  }

  @Test
  public void isTreeTest() throws IOException {
    writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    Assert.assertTrue(TreeUtils.isDirectory("a", tree, repo));
    Assert.assertFalse(TreeUtils.isDirectory("a/b.txt", tree, repo));
    Assert.assertFalse(TreeUtils.isDirectory("a/b", tree, repo));
  }
}
