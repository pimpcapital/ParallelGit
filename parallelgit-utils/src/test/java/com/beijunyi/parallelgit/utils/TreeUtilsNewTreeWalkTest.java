package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TreeUtilsNewTreeWalkTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initRepository();
  }

  @Test
  public void createTreeWalkForTree_shouldReturnNonRecursiveTreeWalk() throws IOException {
    writeMultipleToCache("/a.txt", "/b.txt", "/c/d.txt", "/c/e.txt", "/f/g.txt");
    RevTree tree = commitToMaster().getTree();
    TreeWalk treeWalk = TreeUtils.newTreeWalk(tree, repo);

    assertNextEntry(treeWalk, "a.txt");
    assertNextEntry(treeWalk, "b.txt");
    assertNextEntry(treeWalk, "c");
    assertNextEntry(treeWalk, "f");
    assertFalse(treeWalk.next());
  }

  @Test
  public void createTreeWalkForTreeAndPath_shouldReturnTreeWalkPointingToTheSpecifiedNode() throws IOException {
    writeMultipleToCache("/a.txt", "/b.txt", "/c/d.txt", "/c/e.txt", "/f/g.txt");
    RevTree tree = commitToMaster().getTree();
    TreeWalk treeWalk = TreeUtils.forPath("/c/d.txt", tree, repo);

    assertNotNull(treeWalk);
    assertEquals("d.txt", treeWalk.getNameString());
  }

  @Test
  public void createTreeWalkForTreeAndNonExistentPath_shouldReturnNull() throws IOException {
    writeMultipleToCache("/a.txt", "/b.txt", "/c/d.txt", "/c/e.txt", "/f/g.txt");
    RevTree tree = commitToMaster().getTree();
    TreeWalk treeWalk = TreeUtils.forPath("/non_existent_file.txt", tree, repo);

    assertNull(treeWalk);
  }

  private static void assertNextEntry(TreeWalk treeWalk, String path) throws IOException {
    assertTrue(treeWalk.next());
    assertEquals(path, treeWalk.getPathString());
  }

}
