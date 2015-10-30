package com.beijunyi.parallelgit.filesystem.utils;

import java.net.URI;
import java.nio.file.ProviderMismatchException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitUriUtilsTest {

  @Test(expected = ProviderMismatchException.class)
  public void checkSchemeTest() {
    GitUriUtils.checkScheme(URI.create("/somepath"));
  }

  @Test
  public void getRepository_unixFormat() {
    assertEquals("/", GitUriUtils.getRepository(URI.create("gfs:/")));
    assertEquals("/unix/path", GitUriUtils.getRepository(URI.create("gfs:/unix/path")));
    assertEquals("/unix/path", GitUriUtils.getRepository(URI.create("gfs:/unix/path/")));
    assertEquals("/unix/path", GitUriUtils.getRepository(URI.create("gfs:/unix/path#/a.txt")));
  }

  @Test
  public void getRepository_dosFormat() {
    assertEquals("/c:/", GitUriUtils.getRepository(URI.create("gfs:/c:/")));
    assertEquals("/c:/windows/path", GitUriUtils.getRepository(URI.create("gfs:/c:/windows/path")));
    assertEquals("/c:/windows/path", GitUriUtils.getRepository(URI.create("gfs:/c:/windows/path/")));
    assertEquals("/c:/windows/path", GitUriUtils.getRepository(URI.create("gfs:/c:/windows/path#/a.txt")));
  }

  @Test
  public void getFile() {
    assertEquals("/", GitUriUtils.getFile(URI.create("gfs:/repo#/")));
    assertEquals("/", GitUriUtils.getFile(URI.create("gfs:/repo#")));
    assertEquals("/a.txt", GitUriUtils.getFile(URI.create("gfs:/repo#/a.txt")));
    assertEquals("/a/b.txt", GitUriUtils.getFile(URI.create("gfs:/repo#/a/b.txt")));
    assertEquals("/a/b.txt", GitUriUtils.getFile(URI.create("gfs:/repo#a/b.txt")));
    assertEquals("/a/b", GitUriUtils.getFile(URI.create("gfs:/repo#a/b")));
    assertEquals("/a/b", GitUriUtils.getFile(URI.create("gfs:/repo#/a/b/")));
  }



}
