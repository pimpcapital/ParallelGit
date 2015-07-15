package com.beijunyi.parallelgit.filesystem.utils;

import java.net.URI;
import java.nio.file.ProviderMismatchException;

import org.junit.Assert;
import org.junit.Test;

public class GitUriUtilsTest {

  @Test(expected = ProviderMismatchException.class)
  public void checkSchemeTest() {
    GitUriUtils.checkScheme(URI.create("/somepath"));
  }

  @Test
  public void getRepositoryUnixFormat() {
    Assert.assertEquals("/unix/path", GitUriUtils.getRepository(URI.create("gfs:/unix/path")));
    Assert.assertEquals("/unix/path", GitUriUtils.getRepository(URI.create("gfs:/unix/path/")));
    Assert.assertEquals("/unix/path", GitUriUtils.getRepository(URI.create("gfs:/unix/path#/a.txt")));
  }

  @Test
  public void getRepositoryDosFormat() {
    Assert.assertEquals("/c:/windows/path", GitUriUtils.getRepository(URI.create("gfs:/c:/windows/path")));
    Assert.assertEquals("/c:/windows/path", GitUriUtils.getRepository(URI.create("gfs:/c:/windows/path/")));
    Assert.assertEquals("/c:/windows/path", GitUriUtils.getRepository(URI.create("gfs:/c:/windows/path#/a.txt")));
  }

  @Test
  public void getFile() {
    Assert.assertEquals("/", GitUriUtils.getFile(URI.create("gfs:/repo#/")));
    Assert.assertEquals("/", GitUriUtils.getFile(URI.create("gfs:/repo#")));
    Assert.assertEquals("/a.txt", GitUriUtils.getFile(URI.create("gfs:/repo#/a.txt")));
    Assert.assertEquals("/a/b.txt", GitUriUtils.getFile(URI.create("gfs:/repo#/a/b.txt")));
    Assert.assertEquals("/a/b.txt", GitUriUtils.getFile(URI.create("gfs:/repo#a/b.txt")));
    Assert.assertEquals("/a/b", GitUriUtils.getFile(URI.create("gfs:/repo#a/b")));
    Assert.assertEquals("/a/b", GitUriUtils.getFile(URI.create("gfs:/repo#/a/b/")));
  }



}
