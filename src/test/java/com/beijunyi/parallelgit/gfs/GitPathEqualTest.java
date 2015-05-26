package com.beijunyi.parallelgit.gfs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathEqualTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() {
    initGitFileSystem();
    preventDestroyFileSystem();
  }

  @Test
  public void hashCodeOfAbsolutePathTest() {
    GitPath p1 = gfs.getPath("/a/b/c");
    GitPath p2 = gfs.getPath("/a/b/c");
    Assert.assertTrue(p1.equals(p2));
  }

  @Test
  public void hashCodeOfRelativePathTest() {
    GitPath p1 = gfs.getPath("a/b/c");
    GitPath p2 = gfs.getPath("a/b/c");
    Assert.assertTrue(p1.equals(p2));
  }

  @Test
  public void hashCodesFromDifferentPathsTest() {
    GitPath path = gfs.getPath("/a/b/c");
    Assert.assertFalse(path.equals(gfs.getPath("a/b/c")));
    Assert.assertFalse(path.equals(gfs.getPath("/a/b")));
    Assert.assertFalse(path.equals(gfs.getPath("/a/b/c/d")));
    Assert.assertFalse(path.equals(gfs.getPath("abc")));
    Assert.assertFalse(path.equals(gfs.getPath("/")));
    Assert.assertFalse(path.equals(gfs.getPath("")));
  }

  @Test
  public void hashCodesFromDifferentFileSystemTest() {
    GitFileSystem other = GitFileSystems.newFileSystem(repo);
    GitPath p1 = gfs.getPath("/a/b/c");
    GitPath p2 = other.getPath("/a/b/c");
    Assert.assertFalse(p1.equals(p2));
  }

}
