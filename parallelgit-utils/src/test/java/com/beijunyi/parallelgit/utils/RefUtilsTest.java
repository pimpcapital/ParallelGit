package com.beijunyi.parallelgit.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RefUtilsTest {

  @Test
  public void ensureBranchRefNameWhenInputIsShortName_shouldReturnTheFullRefName() {
    assertEquals("refs/heads/test", RefUtils.fullBranchName("test"));
  }
  @Test
  public void ensureBranchRefNameWhenInputIsFullRefName_shouldReturnTheFullRefName() {
    assertEquals("refs/heads/test", RefUtils.fullBranchName("refs/heads/test"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void ensureBranchRefNameWhenInputIsTag_shouldThrowIllegalArgumentException() {
    RefUtils.fullBranchName("refs/tags/test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void ensureBranchRefNameWhenInputHasSpecialCharacter_shouldThrowIllegalArgumentException() {
    RefUtils.fullBranchName("test?");
  }

}
