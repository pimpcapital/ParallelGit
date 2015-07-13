package com.beijunyi.parallelgit.filesystems;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class GitUriParamsTest {

  @Test
  public void getSessionParamTest() {
    String testSession = "testsession";
    GitUriParams params = GitUriParams.getParams(URI.create("gfs:/repo?session=" + testSession));
    Assert.assertEquals(testSession, params.getSession());
  }


  @Test
  public void getBranchParamTest() {
    String testBranch = "testbranch";
    GitUriParams params = GitUriParams.getParams(URI.create("gfs:/repo?branch=" + testBranch));
    Assert.assertEquals(testBranch, params.getBranch());
  }

  @Test
  public void getRevisionParamTest() {
    String testRevision = "testrevision";
    GitUriParams params = GitUriParams.getParams(URI.create("gfs:/repo?revision=" + testRevision));
    Assert.assertEquals(testRevision, params.getRevision());
  }

  @Test
  public void getTreeParamTest() {
    String testTree = "testtree";
    GitUriParams params = GitUriParams.getParams(URI.create("gfs:/repo?tree=" + testTree));
    Assert.assertEquals(testTree, params.getTree());
  }

}
