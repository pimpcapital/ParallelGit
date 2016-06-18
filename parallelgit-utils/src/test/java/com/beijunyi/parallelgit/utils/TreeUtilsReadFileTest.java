package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;


public class TreeUtilsReadFileTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void readFile_theResultShouldEqualToTheFileContent() throws IOException {
    byte[] expected = someBytes();
    writeToCache("/test_file.txt", expected);
    RevCommit commit = commitToMaster();
    byte[] actual = TreeUtils.readFile("/test_file.txt", commit.getTree(), repo).getData();
    assertArrayEquals(expected, actual);
  }

  @Test(expected = NoSuchFileException.class)
  public void readFileWhenFileDoesNotExist_shouldThrowNoSuchFileException() throws IOException {
    writeSomethingToCache();
    RevCommit commit = commitToMaster();
    TreeUtils.readFile("/non_existent_file.txt", commit.getTree(), repo);
  }


}
