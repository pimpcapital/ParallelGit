package com.beijunyi.parallelgit.filesystem.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsParamsTest {

  @Test
  public void setBranch_theResultParamsShouldContainTheInputBranch() {
    GfsParams params = GfsParams.emptyMap().setBranch("test_branch");
    assertEquals("test_branch", params.getBranch());
  }

  @Test
  public void setCommit_theResultParamsShouldContainTheInputCommit() {
    GfsParams params = GfsParams.emptyMap().setCommit("test_commit");
    assertEquals("test_commit", params.getCommit());
  }

}
