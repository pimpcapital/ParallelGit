package com.beijunyi.parallelgit.gfs;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileSystemGetPathTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void getAbsolutePathTest() {
    GitPath path = gfs.getPath("/a");
    Assert.assertEquals("/a", path.toString());
  }

  @Test
  public void getAbsolutePathWithAdditionalNamesTest() {
    GitPath path = gfs.getPath("/a", "b", "c");
    Assert.assertEquals("/a/b/c", path.toString());
  }

  @Test
  public void getAbsolutePathWithEmptyAdditionalNamesTest() {
    GitPath path = gfs.getPath("/a", "", "b", "");
    Assert.assertEquals("/a/b", path.toString());
  }


  @Test
  public void getRootPathTest() {
    GitPath path = gfs.getPath("/");
    Assert.assertEquals("/", path.toString());
  }

  @Test
  public void getRelativePathTest() {
    GitPath path = gfs.getPath("a");
    Assert.assertEquals("a", path.toString());
  }

  @Test
  public void getRelativePathWithAdditionalNamesTest() {
    GitPath path = gfs.getPath("a", "b", "c");
    Assert.assertEquals("a/b/c", path.toString());
  }

  @Test
  public void getRelativePathWithEmptyAdditionalNamesTest() {
    GitPath path = gfs.getPath("a", "", "b", "");
    Assert.assertEquals("a/b", path.toString());
  }

  @Test
  public void getEmptyPathTest() {
    GitPath path = gfs.getPath("");
    Assert.assertEquals("", path.toString());
  }
}
