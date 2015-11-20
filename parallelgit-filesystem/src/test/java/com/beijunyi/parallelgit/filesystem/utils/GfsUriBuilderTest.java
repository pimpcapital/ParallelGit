package com.beijunyi.parallelgit.filesystem.utils;

import java.net.URI;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsUriBuilderTest {

  @Test
  public void createUri_unixRepoPath() {
    assertEquals(URI.create("gfs:/repo"), GfsUriBuilder.prepare()
                                            .repository("/repo")
                                            .build());
  }

  @Test
  public void createUri_dosRepoPath() {
    assertEquals(URI.create("gfs:/c:/repo"), GfsUriBuilder.prepare()
                                               .repository("/c:/repo")
                                               .build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void createUri_nullRepoPath() {
    GfsUriBuilder.prepare().build();
  }

  @Test
  public void createUriWithFile() {
    assertEquals(URI.create("gfs:/repo#/file.txt"), GfsUriBuilder.prepare()
                                                      .repository("/repo")
                                                      .file("/file.txt")
                                                      .build());
  }

  @Test
  public void createUriWithFile_relativeFilePath() {
    assertEquals(URI.create("gfs:/repo#/file.txt"), GfsUriBuilder.prepare()
                                                      .repository("/repo")
                                                      .file("file.txt")
                                                      .build());
  }

  @Test
  public void createUriWithEmptyFile() {
    assertEquals(URI.create("gfs:/repo"), GfsUriBuilder.prepare()
                                            .repository("/repo")
                                            .file("")
                                            .build());
  }

  @Test
  public void createUriWithRootFile() {
    assertEquals(URI.create("gfs:/repo"), GfsUriBuilder.prepare()
                                            .repository("/repo")
                                            .file("/")
                                            .build());
  }

  @Test
  public void createUriWithSid() {
    assertEquals(URI.create("gfs:/repo?sid=testsession"), GfsUriBuilder.prepare()
                                                            .repository("/repo")
                                                            .sid("testsession")
                                                            .build());
  }

  @Test
  public void createUriWithFileAndSid() {
    assertEquals(URI.create("gfs:/repo?sid=testsession#/file.txt"), GfsUriBuilder.prepare()
                                                                      .repository("/repo")
                                                                      .file("/file.txt")
                                                                      .sid("testsession")
                                                                      .build());
  }

}
