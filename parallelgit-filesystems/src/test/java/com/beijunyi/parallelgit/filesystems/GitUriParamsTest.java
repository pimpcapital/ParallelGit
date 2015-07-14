package com.beijunyi.parallelgit.filesystems;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class GitUriParamsTest {

  @Test
  public void getSessionParamTest() {
    GitUriParams params = GitUriParams.getParams(URI.create("gfs:/repo?session=testsession"));
    Assert.assertEquals("testsession", params.getSession());
  }


  @Test
  public void getBranchParamTest() {
    GitUriParams params = GitUriParams.getParams(URI.create("gfs:/repo?branch=testbranch"));
    Assert.assertEquals("testbranch", params.getBranch());
  }

  @Test
  public void getRevisionParamTest() {
    GitUriParams params = GitUriParams.getParams(URI.create("gfs:/repo?revision=testrevision"));
    Assert.assertEquals("testrevision", params.getRevision());
  }

  @Test
  public void getTreeParamTest() {
    GitUriParams params = GitUriParams.getParams(URI.create("gfs:/repo?tree=testtree"));
    Assert.assertEquals("testtree", params.getTree());
  }

}
