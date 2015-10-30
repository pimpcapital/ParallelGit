package com.beijunyi.parallelgit.filesystem.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitParamsTest {

  @Test
  public void setCreate_theResultParamsShouldContainTheInputCreateOption() {
    GitParams params = GitParams.emptyMap().setCreate(true);
    assertEquals(true, params.getCreate());
  }

  @Test
  public void setBare_theResultParamsShouldContainTheInputBareOption() {
    GitParams params = GitParams.emptyMap().setBare(true);
    assertEquals(true, params.getBare());
  }

  @Test
  public void setBranch_theResultParamsShouldContainTheInputBranch() {
    GitParams params = GitParams.emptyMap().setBranch("test_branch");
    assertEquals("test_branch", params.getBranch());
  }

  @Test
  public void setCommit_theResultParamsShouldContainTheInputCommit() {
    GitParams params = GitParams.emptyMap().setCommit("test_commit");
    assertEquals("test_commit", params.getCommit());
  }

  @Test
  public void setTree_theResultParamsShouldContainTheInputTree() {
    GitParams params = GitParams.emptyMap().setTree("test_tree");
    assertEquals("test_tree", params.getTree());
  }

}
