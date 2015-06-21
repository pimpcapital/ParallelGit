package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathGetParentTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathRootLevelFileGetParentTest() {
    GitPath path = gfs.getPath("/a");
    Assert.assertEquals(root, path.getParent());
  }

  @Test
  public void absolutePathGetParentTest() {
    GitPath a = gfs.getPath("/a");
    GitPath b = gfs.getPath("/a/b");
    GitPath bParent = b.getParent();
    Assert.assertEquals(a, bParent);
  }

  @Test
  public void rootGetParentTest() {
    GitPath root = gfs.getPath("/");
    Assert.assertNull(root.getParent());
  }

  @Test
  public void relativePathGetParentTest() {
    GitPath a = gfs.getPath("a");
    GitPath b = gfs.getPath("a/b");
    GitPath bParent = b.getParent();
    Assert.assertEquals(a, bParent);
  }

  @Test
  public void singleNameRelativePathGetParentTest() {
    GitPath a = gfs.getPath("a");
    Assert.assertNull(a.getParent());
  }

  @Test
  public void emptyPathGetParentTest() {
    GitPath empty = gfs.getPath("");
    Assert.assertNull(empty.getParent());
  }

}
