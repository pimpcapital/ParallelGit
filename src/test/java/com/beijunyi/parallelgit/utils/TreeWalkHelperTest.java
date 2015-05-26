package com.beijunyi.parallelgit.utils;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Assert;
import org.junit.Test;

public class TreeWalkHelperTest extends AbstractParallelGitTest {

  @Test
  public void newTreeWalkTest() {
    initRepository();
    String[] files = new String[] {"a.txt", "b.txt", "c/d.txt", "c/e.txt", "f/g.txt"};
    for(String file : files)
      writeFile(file);
    ObjectId commitId = commitToMaster();
    RevTree tree = RevTreeHelper.getTree(repo, commitId);
    TreeWalk treeWalk = TreeWalkHelper.newTreeWalk(repo, tree);
    String[] rootFiles = new String[] {"a.txt", "b.txt", "c", "f"};
    for(String rootFile : rootFiles) {
      Assert.assertTrue(TreeWalkHelper.next(treeWalk));
      Assert.assertEquals(rootFile, treeWalk.getPathString());
    }
    Assert.assertFalse(TreeWalkHelper.next(treeWalk));
  }

  @Test
  public void forPathTest() {
    initRepository();
    writeFile("a/b.txt");
    ObjectId commit = commitToMaster();
    RevTree root = RevTreeHelper.getTree(repo, commit);
    Assert.assertTrue(TreeWalkHelper.exists(repo, "a", root));
    Assert.assertTrue(TreeWalkHelper.exists(repo, "a/b.txt", root));
    Assert.assertFalse(TreeWalkHelper.exists(repo, "a/b", root));
  }

  @Test
  public void isFileTest() {
    initRepository();
    writeFile("a/b.txt");
    ObjectId commit = commitToMaster();
    RevTree root = RevTreeHelper.getTree(repo, commit);
    Assert.assertFalse(TreeWalkHelper.isFile(repo, "a", root));
    Assert.assertTrue(TreeWalkHelper.isFile(repo, "a/b.txt", root));
    Assert.assertFalse(TreeWalkHelper.isFile(repo, "a/b", root));
  }

  @Test
  public void isDirectoryTest() {
    initRepository();
    writeFile("a/b.txt");
    ObjectId commit = commitToMaster();
    RevTree root = RevTreeHelper.getTree(repo, commit);
    Assert.assertTrue(TreeWalkHelper.isDirectory(repo, "a", root));
    Assert.assertFalse(TreeWalkHelper.isDirectory(repo, "a/b.txt", root));
    Assert.assertFalse(TreeWalkHelper.isDirectory(repo, "a/b", root));
  }
}
