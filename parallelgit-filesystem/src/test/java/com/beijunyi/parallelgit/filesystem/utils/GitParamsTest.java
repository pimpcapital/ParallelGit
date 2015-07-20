package com.beijunyi.parallelgit.filesystem.utils;

import org.junit.Assert;
import org.junit.Test;

public class GitParamsTest {

  @Test
  public void createParam() {
    GitParams params = GitParams.emptyMap().setCreate(true);
    Assert.assertEquals(true, params.getCreate());
  }

  @Test
  public void bareParam() {
    GitParams params = GitParams.emptyMap().setBare(true);
    Assert.assertEquals(true, params.getBare());
  }

  @Test
  public void branchParam() {
    GitParams params = GitParams.emptyMap().setBranch("test_branch");
    Assert.assertEquals("test_branch", params.getBranch());
  }

  @Test
  public void revisionParam() {
    GitParams params = GitParams.emptyMap().setRevision("test_revision");
    Assert.assertEquals("test_revision", params.getRevision());
  }

  @Test
  public void treeParam() {
    GitParams params = GitParams.emptyMap().setTree("test_tree");
    Assert.assertEquals("test_tree", params.getTree());
  }

}
