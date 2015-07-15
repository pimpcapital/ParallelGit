package com.beijunyi.parallelgit.filesystem;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class GitUriBuilderTest {

  @Test
  public void createUri() {
    Assert.assertEquals(URI.create("gfs:/repo"), GitUriBuilder.prepare()
                                                   .repository("/repo")
                                                   .build());
  }

  @Test
  public void createUriWithFile() {
    Assert.assertEquals(URI.create("gfs:/repo!/file.txt"), GitUriBuilder.prepare()
                                                             .repository("/repo")
                                                             .file("/file.txt")
                                                             .build());
  }

  @Test
  public void createUriWithEmptyFile() {
    Assert.assertEquals(URI.create("gfs:/repo"), GitUriBuilder.prepare()
                                                   .repository("/repo")
                                                   .file("")
                                                   .build());
  }

  @Test
  public void createUriWithRootFile() {
    Assert.assertEquals(URI.create("gfs:/repo"), GitUriBuilder.prepare()
                                                   .repository("/repo")
                                                   .file("/")
                                                   .build());
  }

  @Test
  public void createUriWithSessionParam() {
    Assert.assertEquals(URI.create("gfs:/repo?session=testsession"), GitUriBuilder.prepare()
                                                                       .repository("/repo")
                                                                       .session("testsession")
                                                                       .build());
  }

  @Test
  public void createUriWithBranchParam() {
    Assert.assertEquals(URI.create("gfs:/repo?branch=testbranch"), GitUriBuilder.prepare()
                                                                     .repository("/repo")
                                                                     .branch("testbranch")
                                                                     .build());
  }

  @Test
  public void createUriWithRevisionParam() {
    Assert.assertEquals(URI.create("gfs:/repo?revision=testrevision"), GitUriBuilder.prepare()
                                                                         .repository("/repo")
                                                                         .revision("testrevision")
                                                                         .build());
  }

  @Test
  public void createUriWithTreeParam() {
    Assert.assertEquals(URI.create("gfs:/repo?tree=testtree"), GitUriBuilder.prepare()
                                                                 .repository("/repo")
                                                                 .tree("testtree")
                                                                 .build());
  }

  @Test
  public void createUriWithAllParams() {
    URI uri = GitUriBuilder.prepare()
                .repository("/repo")
                .session("testsession")
                .branch("testbranch")
                .revision("testrevision")
                .tree("testtree")
                .build();
    GitUriParams params = GitUriParams.getParams(uri);
    Assert.assertEquals("testsession", params.getSession());
    Assert.assertEquals("testbranch", params.getBranch());
    Assert.assertEquals("testrevision", params.getRevision());
    Assert.assertEquals("testtree", params.getTree());
  }

}
