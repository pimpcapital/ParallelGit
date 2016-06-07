package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Test;

public class ObjectUtilsTest extends AbstractParallelGitTest {

  @Test
  public void findFileBlobIdTest() throws IOException {
    initRepository();
    String file = "a.txt";
    AnyObjectId fileBlobId = writeToCache(file);
    AnyObjectId commit = commitToMaster();
    Assert.assertEquals(fileBlobId, ObjectUtils.findObject(file, commit, repo));
  }

  @Test
  public void findNonExistentFileBlobIdTest() throws IOException {
    AnyObjectId commit = initRepository();
    Assert.assertNull(ObjectUtils.findObject("non-existent.txt", commit, repo));
  }

}
