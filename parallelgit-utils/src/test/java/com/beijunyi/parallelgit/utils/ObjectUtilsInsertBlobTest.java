package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ObjectUtilsInsertBlobTest extends AbstractParallelGitTest {

  @Test
  public void insertBlobIntoRepository_shouldBeAbleToRetrieveByteArrayByBlobId() throws IOException {
    initRepository();

    byte[] bytes = someBytes();
    ObjectId blob = ObjectUtils.insertBlob(bytes, repo);

    assertArrayEquals(bytes, ObjectUtils.readBlob(blob, repo).getData());
  }

}
