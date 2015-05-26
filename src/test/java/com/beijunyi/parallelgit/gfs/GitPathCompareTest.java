package com.beijunyi.parallelgit.gfs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathCompareTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() {
    initGitFileSystem();
    preventDestroyFileSystem();
  }

  @Test
  public void absolutePathCompareTest() {
    GitPath p1 = gfs.getPath("/a/b/1");
    GitPath p2 = gfs.getPath("/a/b/2");
    Assert.assertTrue(p1.compareTo(p2) < 0);
    Assert.assertTrue(p2.compareTo(p1) > 0);
  }

  @Test
  public void absolutePathCompareToSamePathTest() {
    GitPath p1 = gfs.getPath("/a/b/1");
    GitPath p2 = gfs.getPath("/a/b/1");
    Assert.assertTrue(p1.compareTo(p2) == 0);
  }

  @Test
  public void absolutePathCompareToPrefixPathTest() {
    GitPath p1 = gfs.getPath("/a/b/1");
    GitPath p2 = gfs.getPath("/a/b/11");
    Assert.assertTrue(p1.compareTo(p2) < 0);
    Assert.assertTrue(p2.compareTo(p1) > 0);
  }

  @Test
  public void relativePathCompareTest() {
    GitPath p1 = gfs.getPath("a/b/1");
    GitPath p2 = gfs.getPath("a/b/2");
    Assert.assertTrue(p1.compareTo(p2) < 0);
    Assert.assertTrue(p2.compareTo(p1) > 0);
  }

  @Test
  public void relativePathCompareToSamePathTest() {
    GitPath p1 = gfs.getPath("a/b/1");
    GitPath p2 = gfs.getPath("a/b/1");
    Assert.assertTrue(p1.compareTo(p2) == 0);
  }

  @Test
  public void relativePathCompareToPrefixPathTest() {
    GitPath p1 = gfs.getPath("a/b/1");
    GitPath p2 = gfs.getPath("a/b/11");
    Assert.assertTrue(p1.compareTo(p2) < 0);
    Assert.assertTrue(p2.compareTo(p1) > 0);
  }
}
