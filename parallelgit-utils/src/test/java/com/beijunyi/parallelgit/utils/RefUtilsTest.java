package com.beijunyi.parallelgit.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RefUtilsTest {

  @Test
  public void ensureBranchRefNameWhenInputIsShortName_shouldReturnTheFullRefName() {
    assertEquals("refs/heads/test", RefUtils.ensureBranchRefName("test"));
  }
  @Test
  public void ensureBranchRefNameWhenInputIsFullRefName_shouldReturnTheFullRefName() {
    assertEquals("refs/heads/test", RefUtils.ensureBranchRefName("refs/heads/test"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void ensureBranchRefNameWhenInputIsTag_shouldThrowIllegalArgumentException() {
    RefUtils.ensureBranchRefName("refs/tags/test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void ensureBranchRefNameWhenInputHasSpecialCharacter_shouldThrowIllegalArgumentException() {
    RefUtils.ensureBranchRefName("test?");
  }

}
