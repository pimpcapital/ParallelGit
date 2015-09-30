package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileUtilsReadFileTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void readFile_theResultShoudEqualToTheFileContent() throws IOException {
    byte[] expected = "test data".getBytes();
    writeToCache("/test_file.txt", expected);
    AnyObjectId commit = commitToMaster();
    byte[] actual = GitFileUtils.readFile("/test_file.txt", commit.getName(), repo);
    Assert.assertArrayEquals(expected, actual);
  }

  @Test
  public void readNonExistentFile_theResultShouldBeNull() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    Assert.assertNull(GitFileUtils.readFile("/non_existent_file.txt", commit.getName(), repo));
  }

}
