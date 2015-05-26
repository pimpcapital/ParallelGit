package com.beijunyi.parallelgit.gfs;

import java.io.IOException;

import com.beijunyi.parallelgit.ParallelGitException;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemExceptionTest {

  @Test
  public void exceptionTypeTest() {
    Assert.assertTrue(RuntimeException.class.isAssignableFrom(GitFileSystemException.class));
    Assert.assertTrue(ParallelGitException.class.isAssignableFrom(GitFileSystemException.class));
  }

  @Test
  public void exceptionWithMessageTest() {
    String msg = "error message";
    GitFileSystemException exception = new GitFileSystemException(msg);
    Assert.assertEquals(msg, exception.getMessage());
  }

  @Test
  public void exceptionWithMessageAndCauseTest() {
    String msg = "error message";
    Exception cause = new IOException();
    GitFileSystemException exception = new GitFileSystemException(msg, cause);
    Assert.assertEquals(msg, exception.getMessage());
    Assert.assertEquals(cause, exception.getCause());
  }
}
