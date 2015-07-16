package com.beijunyi.parallelgit.filesystem.utils;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class GitUriBuilderTest {

  @Test
  public void createUri_unixRepoPath() {
    Assert.assertEquals(URI.create("gfs:/repo"), GitUriBuilder.prepare()
                                                   .repository("/repo")
                                                   .build());
  }

  @Test
  public void createUri_dosRepoPath() {
    Assert.assertEquals(URI.create("gfs:/c:/repo"), GitUriBuilder.prepare()
                                                   .repository("/c:/repo")
                                                   .build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void createUri_nullRepoPath() {
    GitUriBuilder.prepare().build();
  }

  @Test
  public void createUriWithFile() {
    Assert.assertEquals(URI.create("gfs:/repo#/file.txt"), GitUriBuilder.prepare()
                                                             .repository("/repo")
                                                             .file("/file.txt")
                                                             .build());
  }

  @Test
  public void createUriWithFile_relativeFilePath() {
    Assert.assertEquals(URI.create("gfs:/repo#/file.txt"), GitUriBuilder.prepare()
                                                             .repository("/repo")
                                                             .file("file.txt")
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
  public void createUriWithSid() {
    Assert.assertEquals(URI.create("gfs:/repo?sid=testsession"), GitUriBuilder.prepare()
                                                                       .repository("/repo")
                                                                       .sid("testsession")
                                                                       .build());
  }

  @Test
  public void createUriWithFileAndSid() {
    Assert.assertEquals(URI.create("gfs:/repo?sid=testsession#/file.txt"), GitUriBuilder.prepare()
                                                                   .repository("/repo")
                                                                   .file("/file.txt")
                                                                   .sid("testsession")
                                                                   .build());
  }

}
