package com.beijunyi.parallelgit.utils;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class BlobHelperTest extends AbstractParallelGitTest {

  @Test
  public void findFileBlobIdTest() {
    initRepository();
    String file = "a.txt";
    ObjectId fileBlob = writeFile(file);
    ObjectId commit = commitToMaster();
    Assert.assertEquals(fileBlob, BlobHelper.findBlobId(repo, file, commit));
  }

  @Test
  public void findNonExistentFileBlobIdTest() {
    ObjectId commit = initRepository();
    Assert.assertNull(BlobHelper.findBlobId(repo, "non-existent.txt", commit));
  }

}
