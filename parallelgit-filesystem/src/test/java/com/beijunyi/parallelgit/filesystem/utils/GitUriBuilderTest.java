package com.beijunyi.parallelgit.filesystem.utils;

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

}
