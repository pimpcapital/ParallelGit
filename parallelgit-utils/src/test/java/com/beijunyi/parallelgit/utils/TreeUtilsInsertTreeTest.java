package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.TreeFormatter;
import org.junit.Test;

import static org.eclipse.jgit.lib.FileMode.*;
import static org.junit.Assert.assertEquals;

public class TreeUtilsInsertTreeTest extends AbstractParallelGitTest {

  @Test
  public void insertTreeIntoRepository_shouldBeAbleToRetrieveChildrenIdsByTreeIdAndFilename() throws IOException {
    initRepository();

    TreeFormatter tf = new TreeFormatter();
    ObjectId nodeObject1 = someObjectId();
    tf.append("file1.txt", REGULAR_FILE, nodeObject1);
    ObjectId nodeObject2 = someObjectId();
    tf.append("file2.txt", REGULAR_FILE, nodeObject2);
    ObjectId tree = TreeUtils.insertTree(tf, repo);

    assertEquals(nodeObject1, TreeUtils.getObjectId("file1.txt", tree, repo));
    assertEquals(nodeObject2, TreeUtils.getObjectId("file2.txt", tree, repo));
  }

  @Test
  public void insertTreeIntoRepository_shouldBeAbleToRetrieveChildrenFileModesByTreeIdAndFilename() throws IOException {
    initRepository();

    TreeFormatter tf = new TreeFormatter();
    tf.append("file.txt", REGULAR_FILE, someObjectId());
    tf.append("dir", TREE, someObjectId());
    ObjectId tree = TreeUtils.insertTree(tf, repo);

    assertEquals(REGULAR_FILE, TreeUtils.getFileMode("file.txt", tree, repo));
    assertEquals(TREE, TreeUtils.getFileMode("dir", tree, repo));
  }

}
