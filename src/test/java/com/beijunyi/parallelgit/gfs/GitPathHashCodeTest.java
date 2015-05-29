package com.beijunyi.parallelgit.gfs;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathHashCodeTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
    preventDestroyFileSystem();
  }

  @Test
  public void hashCodeOfAbsolutePathTest() {
    GitPath p1 = gfs.getPath("/a/b/c");
    GitPath p2 = gfs.getPath("/a/b/c");
    Assert.assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void hashCodeOfRelativePathTest() {
    GitPath p1 = gfs.getPath("a/b/c");
    GitPath p2 = gfs.getPath("a/b/c");
    Assert.assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void hashCodesFromDifferentPathsTest() {
    GitPath path = gfs.getPath("/a/b/c");
    int hashCode = path.hashCode();
    Assert.assertNotEquals(hashCode, gfs.getPath("a/b/c").hashCode());
    Assert.assertNotEquals(hashCode, gfs.getPath("/a/b").hashCode());
    Assert.assertNotEquals(hashCode, gfs.getPath("/a/b/c/d").hashCode());
    Assert.assertNotEquals(hashCode, gfs.getPath("abc").hashCode());
    Assert.assertNotEquals(hashCode, gfs.getPath("/").hashCode());
    Assert.assertNotEquals(hashCode, gfs.getPath("").hashCode());
  }

  @Test
  public void hashCodesFromDifferentFileSystemTest() throws IOException {
    GitFileSystem other = GitFileSystems.newFileSystem(repo);
    GitPath p1 = gfs.getPath("/a/b/c");
    GitPath p2 = other.getPath("/a/b/c");
    Assert.assertNotEquals(p1.hashCode(), p2.hashCode());
  }

}
