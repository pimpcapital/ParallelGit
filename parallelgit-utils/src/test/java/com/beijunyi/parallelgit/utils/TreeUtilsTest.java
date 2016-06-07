package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TreeUtilsTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initRepository();
  }

  @Test
  public void existsTest() throws IOException {
    writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    assertTrue(TreeUtils.exists("a", tree, repo));
    assertTrue(TreeUtils.exists("a/b.txt", tree, repo));
    assertFalse(TreeUtils.exists("a/b", tree, repo));
  }

  @Test
  public void getObjectIdTest() throws IOException {
    AnyObjectId objectId = writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    assertEquals(objectId, TreeUtils.getObjectId("a/b.txt", tree, repo));
  }

  @Test
  public void isDirectoryTest() throws IOException {
    writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    assertTrue(TreeUtils.isDirectory("a", tree, repo));
    assertFalse(TreeUtils.isDirectory("a/b.txt", tree, repo));
    assertFalse(TreeUtils.isDirectory("a/b", tree, repo));
  }

  private static void assertNextEntry(TreeWalk treeWalk, String path) throws IOException {
    assertTrue(treeWalk.next());
    assertEquals(path, treeWalk.getPathString());
  }

}
