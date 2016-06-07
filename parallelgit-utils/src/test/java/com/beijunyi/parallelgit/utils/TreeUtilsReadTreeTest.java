package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TreeUtilsReadTreeTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void readTree_theResultShouldHaveTheChildrenNames() throws Exception {
    writeToCache("/dir/file3.txt");
    writeToCache("/file1.txt");
    writeToCache("/file2.txt");
    ObjectId tree = commit().getTree();

    List<String> actual = new ArrayList<>();
    TreeSnapshot snapshot = TreeUtils.readTree(tree, repo);
    for(String file : snapshot.getData().keySet())
      actual.add(file);

    assertEquals(tree, snapshot.getId());
    assertEquals("dir", actual.get(0));
    assertEquals("file1.txt", actual.get(1));
    assertEquals("file2.txt", actual.get(2));
  }

}
