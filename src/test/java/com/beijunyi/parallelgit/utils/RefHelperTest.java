package com.beijunyi.parallelgit.utils;

import com.beijunyi.parallelgit.ParallelGitException;
import org.junit.Assert;
import org.junit.Test;

public class RefHelperTest {

  @Test
  public void getBranchRefNameTest() {
    Assert.assertEquals("refs/heads/test", RefHelper.getBranchRefName("test"));
    Assert.assertEquals("refs/heads/test", RefHelper.getBranchRefName("refs/heads/test"));
  }

  @Test(expected = ParallelGitException.class)
  public void getInvalidBranchRefNameTest() {
    RefHelper.getBranchRefName("test?");
  }

}
