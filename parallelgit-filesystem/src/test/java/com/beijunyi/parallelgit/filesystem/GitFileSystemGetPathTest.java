package com.beijunyi.parallelgit.filesystem;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitFileSystemGetPathTest extends PreSetupGitFileSystemTest {

  @Test
  public void getAbsolutePathTest() {
    GitPath path = gfs.getPath("/a");
    assertEquals("/a", path.toString());
  }

  @Test
  public void getAbsolutePathWithAdditionalNamesTest() {
    GitPath path = gfs.getPath("/a", "b", "c");
    assertEquals("/a/b/c", path.toString());
  }

  @Test
  public void getAbsolutePathWithEmptyAdditionalNamesTest() {
    GitPath path = gfs.getPath("/a", "", "b", "");
    assertEquals("/a/b", path.toString());
  }


  @Test
  public void getRootPathTest() {
    GitPath path = gfs.getPath("/");
    assertEquals("/", path.toString());
  }

  @Test
  public void getRelativePathTest() {
    GitPath path = gfs.getPath("a");
    assertEquals("a", path.toString());
  }

  @Test
  public void getRelativePathWithAdditionalNamesTest() {
    GitPath path = gfs.getPath("a", "b", "c");
    assertEquals("a/b/c", path.toString());
  }

  @Test
  public void getRelativePathWithEmptyAdditionalNamesTest() {
    GitPath path = gfs.getPath("a", "", "b", "");
    assertEquals("a/b", path.toString());
  }

  @Test
  public void getEmptyPathTest() {
    GitPath path = gfs.getPath("");
    assertEquals("", path.toString());
  }
}
