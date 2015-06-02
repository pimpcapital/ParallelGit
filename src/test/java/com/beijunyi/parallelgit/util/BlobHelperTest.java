package com.beijunyi.parallelgit.util;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class BlobHelperTest extends AbstractParallelGitTest {

  @Test
  public void findFileBlobIdTest() throws IOException {
    initRepository();
    String file = "a.txt";
    ObjectId fileBlob = writeFile(file);
    ObjectId commit = commitToMaster();
    Assert.assertEquals(fileBlob, BlobHelper.findBlobId(repo, file, commit));
  }

  @Test
  public void findNonExistentFileBlobIdTest() throws IOException {
    ObjectId commit = initRepository();
    Assert.assertNull(BlobHelper.findBlobId(repo, "non-existent.txt", commit));
  }

}
