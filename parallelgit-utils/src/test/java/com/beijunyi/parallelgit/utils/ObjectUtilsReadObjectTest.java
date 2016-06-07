package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
      assertEquals(actual.length, input.read(actual));
      assertArrayEquals(expected, actual);
    }
  }

  @Test
  public void readObject_theResultShouldEqualToTheObjectData() throws Exception {
    byte[] expected = "test text data".getBytes();
    ObjectId objectId = ObjectUtils.insertBlob(expected, repo);
    assertArrayEquals(expected, ObjectUtils.readBlob(objectId, repo).getData());
  }

}
