package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ObjectUtilsReadObjectTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void openObject_theResultInputStreamShouldProvideTheObjectData() throws Exception {
    byte[] expected = "test text data".getBytes();
    AnyObjectId objectId = ObjectUtils.insertBlob(expected, repo);
    try(InputStream input = ObjectUtils.openBlob(objectId, repo)) {
      byte[] actual = new byte[expected.length];
      Assert.assertEquals(actual.length, input.read(actual));
      Assert.assertArrayEquals(expected, actual);
    }
  }

  @Test
  public void readObject_theResultShouldEqualToTheObjectData() throws Exception {
    byte[] expected = "test text data".getBytes();
    AnyObjectId objectId = ObjectUtils.insertBlob(expected, repo);
    Assert.assertArrayEquals(expected, ObjectUtils.readBlob(objectId, repo).getData());
  }

}
