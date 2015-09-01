package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Test;

public class BlobHelperTest extends AbstractParallelGitTest {

  @Test
  public void findFileBlobIdTest() throws IOException {
    initRepository();
    String file = "a.txt";
    AnyObjectId fileBlob = writeToCache(file);
    AnyObjectId commit = commitToMaster();
    Assert.assertEquals(fileBlob, BlobHelper.findBlobId(file, commit, repo));
  }

  @Test
  public void findNonExistentFileBlobIdTest() throws IOException {
    AnyObjectId commit = initRepository();
    Assert.assertNull(BlobHelper.findBlobId("non-existent.txt", commit, repo));
  }

}
