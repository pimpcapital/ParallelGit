package com.beijunyi.parallelgit.filesystem.utils;

import java.net.URI;
import java.nio.file.ProviderMismatchException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsUriUtilsTest {

  @Test(expected = ProviderMismatchException.class)
  public void checkSchemeTest() {
    GfsUriUtils.checkScheme(URI.create("/somepath"));
  }

  @Test
  public void getRepository_unixFormat() {
    assertEquals("/", GfsUriUtils.getRepository(URI.create("gfs:/")));
    assertEquals("/unix/path", GfsUriUtils.getRepository(URI.create("gfs:/unix/path")));
    assertEquals("/unix/path", GfsUriUtils.getRepository(URI.create("gfs:/unix/path/")));
    assertEquals("/unix/path", GfsUriUtils.getRepository(URI.create("gfs:/unix/path#/a.txt")));
  }

  @Test
  public void getRepository_dosFormat() {
    assertEquals("/c:/", GfsUriUtils.getRepository(URI.create("gfs:/c:/")));
    assertEquals("/c:/windows/path", GfsUriUtils.getRepository(URI.create("gfs:/c:/windows/path")));
    assertEquals("/c:/windows/path", GfsUriUtils.getRepository(URI.create("gfs:/c:/windows/path/")));
    assertEquals("/c:/windows/path", GfsUriUtils.getRepository(URI.create("gfs:/c:/windows/path#/a.txt")));
  }

  @Test
  public void getFile() {
    assertEquals("/", GfsUriUtils.getFile(URI.create("gfs:/repo#/")));
    assertEquals("/", GfsUriUtils.getFile(URI.create("gfs:/repo#")));
    assertEquals("/a.txt", GfsUriUtils.getFile(URI.create("gfs:/repo#/a.txt")));
    assertEquals("/a/b.txt", GfsUriUtils.getFile(URI.create("gfs:/repo#/a/b.txt")));
    assertEquals("/a/b.txt", GfsUriUtils.getFile(URI.create("gfs:/repo#a/b.txt")));
    assertEquals("/a/b", GfsUriUtils.getFile(URI.create("gfs:/repo#a/b")));
    assertEquals("/a/b", GfsUriUtils.getFile(URI.create("gfs:/repo#/a/b/")));
  }



}
