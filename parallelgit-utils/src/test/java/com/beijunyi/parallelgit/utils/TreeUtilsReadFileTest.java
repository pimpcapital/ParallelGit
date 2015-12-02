package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TreeUtilsReadFileTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void readFile_theResultShouldEqualToTheFileContent() throws IOException {
    byte[] expected = "test data".getBytes();
    writeToCache("/test_file.txt", expected);
    RevCommit commit = commitToMaster();
    byte[] actual = TreeUtils.readFile("/test_file.txt", commit.getTree(), repo).getBytes();
    Assert.assertArrayEquals(expected, actual);
  }

  @Test(expected = NoSuchFileException.class)
  public void readFileWhenFileDoesNotExist_shouldThrowNoSuchFileException() throws IOException {
    writeSomeFileToCache();
    RevCommit commit = commitToMaster();
    TreeUtils.readFile("/non_existent_file.txt", commit.getTree(), repo);
  }


}