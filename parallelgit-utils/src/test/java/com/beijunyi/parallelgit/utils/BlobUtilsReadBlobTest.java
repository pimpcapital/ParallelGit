package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BlobUtilsReadBlobTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void getBlobSize_shouldReturnTheLengthOfTheByteArray() throws IOException {
    byte[] bytes = someBytes();
    ObjectId blob = writeToCache(someFilename(), bytes);
    commit();

    assertEquals(bytes.length, BlobUtils.getBlobSize(blob, repo));
  }

  @Test
  public void openBlob_theResultShouldStreamTheBlobData() throws Exception {
    byte[] expected = someBytes();
    ObjectId blob = writeToCache(someFilename(), expected);
    commit();

    try(InputStream input = BlobUtils.openBlob(blob, repo)) {
      byte[] actual = new byte[expected.length];
      assertEquals(actual.length, input.read(actual));
      assertArrayEquals(expected, actual);
    }
  }

  @Test
  public void readBlob_theResultShouldHaveTheBlobData() throws Exception {
    byte[] expected = someBytes();
    ObjectId blob = writeToCache(someFilename(), expected);
    commit();

    BlobSnapshot snapshot = BlobUtils.readBlob(blob, repo);
    assertEquals(blob, snapshot.getId());
    assertArrayEquals(expected, snapshot.getData());
  }

}
