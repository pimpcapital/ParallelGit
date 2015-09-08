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

  private static void assertNextEntry(@Nonnull TreeWalk treeWalk, @Nonnull String path) throws IOException {
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
    writeFilesToCache("a.txt", "b.txt", "c/d.txt", "c/e.txt", "f/g.txt");
    AnyObjectId commitId = commitToMaster();
    RevTree tree = RevTreeUtils.getRootTree(repo, commitId);
    TreeWalk treeWalk = TreeUtils.newTreeWalk(repo, tree);

    assertNextEntry(treeWalk, "a.txt");
    assertNextEntry(treeWalk, "b.txt");
    assertNextEntry(treeWalk, "c");
    assertNextEntry(treeWalk, "f");
    Assert.assertFalse(treeWalk.next());
  }

  @Test
  public void existsTest() throws IOException {
    writeToCache("a/b.txt");
    AnyObjectId commit = commitToMaster();

    RevTree tree = RevTreeUtils.getRootTree(repo, commit);
    Assert.assertTrue(TreeUtils.exists(repo, "a", tree));
    Assert.assertTrue(TreeUtils.exists(repo, "a/b.txt", tree));
    Assert.assertFalse(TreeUtils.exists(repo, "a/b", tree));
  }

  @Test
  public void getObjectTest() throws IOException {
    AnyObjectId objectId = writeToCache("a/b.txt");
    AnyObjectId commit = commitToMaster();

    RevTree tree = RevTreeUtils.getRootTree(repo, commit);
    Assert.assertEquals(objectId, TreeUtils.getObjectId(repo, "a/b.txt", tree));
  }

  @Test
  public void isBlobTest() throws IOException {
    writeToCache("a/b.txt");
    AnyObjectId commit = commitToMaster();

    RevTree tree = RevTreeUtils.getRootTree(repo, commit);
    Assert.assertFalse(TreeUtils.isFileOrSymbolicLink(repo, "a", tree));
    Assert.assertTrue(TreeUtils.isFileOrSymbolicLink(repo, "a/b.txt", tree));
    Assert.assertFalse(TreeUtils.isFileOrSymbolicLink(repo, "a/b", tree));
  }

  @Test
  public void isTreeTest() throws IOException {
    writeToCache("a/b.txt");
    AnyObjectId commit = commitToMaster();

    RevTree tree = RevTreeUtils.getRootTree(repo, commit);
    Assert.assertTrue(TreeUtils.isDirectory(repo, "a", tree));
    Assert.assertFalse(TreeUtils.isDirectory(repo, "a/b.txt", tree));
    Assert.assertFalse(TreeUtils.isDirectory(repo, "a/b", tree));
  }
}
