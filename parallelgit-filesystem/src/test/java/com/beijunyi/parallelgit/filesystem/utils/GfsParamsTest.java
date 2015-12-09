package com.beijunyi.parallelgit.filesystem.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsParamsTest {

  @Test
  public void setBranch_theResultParamsShouldContainTheInputBranch() {
    GfsParams params = GfsParams.emptyMap().branch("test_branch");
    assertEquals("test_branch", params.branch());
  }

  @Test
  public void setCommit_theResultParamsShouldContainTheInputCommit() {
    GfsParams params = GfsParams.emptyMap().commit("test_commit");
    assertEquals("test_commit", params.commit());
  }

}
