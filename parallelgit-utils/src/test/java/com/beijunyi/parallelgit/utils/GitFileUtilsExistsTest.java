package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileUtilsExistsTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void testFileExistsWhenFileExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    RevCommit commit = commit(null);
    Assert.assertTrue(GitFileUtils.exists("/test_file.txt", commit.getName(), repo));
  }

  @Test
  public void testFileExistsWhenDirectoryExists_shouldReturnTrue() throws IOException {
    writeToCache("/dir/file.txt");
    RevCommit commit = commit(null);
    Assert.assertTrue(GitFileUtils.exists("/dir", commit.getName(), repo));
  }

  @Test
  public void testFileExistsWhenFileDoesNotExist_shouldReturnFalse() throws IOException {
    writeSomeFileToCache();
    RevCommit commit = commit(null);
    Assert.assertFalse(GitFileUtils.exists("/non_existent_file.txt", commit.getName(), repo));
  }


}
