package com.beijunyi.parallelgit.utils;

import org.junit.Assert;
import org.junit.Test;

public class RefUtilsTest {

  @Test
  public void getBranchRefNameTest() {
    Assert.assertEquals("refs/heads/test", RefUtils.ensureBranchRefName("test"));
    Assert.assertEquals("refs/heads/test", RefUtils.ensureBranchRefName("refs/heads/test"));
  }

  @Test(expected = Exception.class)
  public void getInvalidBranchRefNameTest() {
    RefUtils.ensureBranchRefName("test?");
  }

}
