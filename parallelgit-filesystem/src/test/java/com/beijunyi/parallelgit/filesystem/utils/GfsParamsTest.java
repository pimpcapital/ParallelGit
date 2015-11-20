package com.beijunyi.parallelgit.filesystem.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsParamsTest {

  @Test
  public void setCreate_theResultParamsShouldContainTheInputCreateOption() {
    GfsParams params = GfsParams.emptyMap().setCreate(true);
    assertEquals(true, params.getCreate());
  }

  @Test
  public void setBare_theResultParamsShouldContainTheInputBareOption() {
    GfsParams params = GfsParams.emptyMap().setBare(true);
    assertEquals(true, params.getBare());
  }

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

  @Test
  public void setTree_theResultParamsShouldContainTheInputTree() {
    GfsParams params = GfsParams.emptyMap().setTree("test_tree");
    assertEquals("test_tree", params.getTree());
  }

}
