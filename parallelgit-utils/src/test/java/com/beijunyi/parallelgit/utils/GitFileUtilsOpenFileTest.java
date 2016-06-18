package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileUtilsOpenFileTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void openFile_theResultInputStreamShouldProvideTheDataOfTheFile() throws IOException {
    byte[] expected = someBytes();
    writeToCache("/test_file.txt", expected);
    AnyObjectId commit = commitToMaster();
    byte[] actual = new byte[expected.length];
    try(InputStream stream = GitFileUtils.openFile("/test_file.txt", commit.getName(), repo)) {
      assertEquals(expected.length, stream.read(actual));
    }
    assertArrayEquals(expected, actual);
  }

  @Test(expected = NoSuchFileException.class)
  public void openFileWhenFileDoesNotExist_shouldThrowNoSuchFileException() throws IOException {
    writeSomethingToCache();
    AnyObjectId commit = commitToMaster();
    GitFileUtils.openFile("/non_existent_file.txt", commit.getName(), repo);
  }


}
