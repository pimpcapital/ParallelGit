package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TreeUtilsOpenFileTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void openFile_theResultInputStreamShouldProvideTheDataOfTheFile() throws IOException {
    byte[] expected = "test data".getBytes();
    writeToCache("/test_file.txt", expected);
    RevCommit commit = commitToMaster();
    byte[] actual = new byte[expected.length];
    try(InputStream stream = TreeUtils.openFile("/test_file.txt", commit.getTree(), repo)) {
      Assert.assertEquals(expected.length, stream.read(actual));
    }
    Assert.assertArrayEquals(expected, actual);
  }

  @Test(expected = NoSuchFileException.class)
  public void openFileWhenFileDoesNotExist_shouldThrowNoSuchFileException() throws IOException {
    writeSomethingToCache();
    RevCommit commit = commitToMaster();
    TreeUtils.openFile("/non_existent_file.txt", commit.getTree(), repo);
  }


}
