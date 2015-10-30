package com.beijunyi.parallelgit.filesystem.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitParamsTest {

  @Test
  public void createParam() {
    GitParams params = GitParams.emptyMap().setCreate(true);
    assertEquals(true, params.getCreate());
  }

  @Test
  public void bareParam() {
    GitParams params = GitParams.emptyMap().setBare(true);
    assertEquals(true, params.getBare());
  }

  @Test
  public void branchParam() {
    GitParams params = GitParams.emptyMap().setBranch("test_branch");
    assertEquals("test_branch", params.getBranch());
  }

  @Test
  public void revisionParam() {
    GitParams params = GitParams.emptyMap().setRevision("test_revision");
    assertEquals("test_revision", params.getRevision());
  }

  @Test
  public void treeParam() {
    GitParams params = GitParams.emptyMap().setTree("test_tree");
    assertEquals("test_tree", params.getTree());
  }

}
