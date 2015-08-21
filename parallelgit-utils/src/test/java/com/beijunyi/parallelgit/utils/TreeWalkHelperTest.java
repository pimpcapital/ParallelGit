package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TreeWalkHelperTest extends AbstractParallelGitTest {

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
    ObjectId commitId = commitToMaster();
    RevTree tree = RevTreeHelper.getRootTree(repo, commitId);
    TreeWalk treeWalk = TreeWalkHelper.newTreeWalk(repo, tree);

    assertNextEntry(treeWalk, "a.txt");
    assertNextEntry(treeWalk, "b.txt");
    assertNextEntry(treeWalk, "c");
    assertNextEntry(treeWalk, "f");
    Assert.assertFalse(treeWalk.next());
  }

  @Test
  public void existsTest() throws IOException {
    writeToCache("a/b.txt");
    ObjectId commit = commitToMaster();

    RevTree tree = RevTreeHelper.getRootTree(repo, commit);
    Assert.assertTrue(TreeWalkHelper.exists(repo, "a", tree));
    Assert.assertTrue(TreeWalkHelper.exists(repo, "a/b.txt", tree));
    Assert.assertFalse(TreeWalkHelper.exists(repo, "a/b", tree));
  }

  @Test
  public void getObjectTest() throws IOException {
    ObjectId objectId = writeToCache("a/b.txt");
    ObjectId commit = commitToMaster();

    RevTree tree = RevTreeHelper.getRootTree(repo, commit);
    Assert.assertEquals(objectId, TreeWalkHelper.getObject(repo, "a/b.txt", tree));
  }

  @Test
  public void isBlobTest() throws IOException {
    writeToCache("a/b.txt");
    ObjectId commit = commitToMaster();

    RevTree tree = RevTreeHelper.getRootTree(repo, commit);
    Assert.assertFalse(TreeWalkHelper.isFileOrSymbolicLink(repo, "a", tree));
    Assert.assertTrue(TreeWalkHelper.isFileOrSymbolicLink(repo, "a/b.txt", tree));
    Assert.assertFalse(TreeWalkHelper.isFileOrSymbolicLink(repo, "a/b", tree));
  }

  @Test
  public void isTreeTest() throws IOException {
    writeToCache("a/b.txt");
    ObjectId commit = commitToMaster();

    RevTree tree = RevTreeHelper.getRootTree(repo, commit);
    Assert.assertTrue(TreeWalkHelper.isDirectory(repo, "a", tree));
    Assert.assertFalse(TreeWalkHelper.isDirectory(repo, "a/b.txt", tree));
    Assert.assertFalse(TreeWalkHelper.isDirectory(repo, "a/b", tree));
  }
}
