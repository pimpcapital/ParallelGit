package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitPathGetParentTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathRootLevelFileGetParentTest() {
    GitPath path = gfs.getPath("/a");
    assertEquals(root, path.getParent());
  }

  @Test
  public void absolutePathGetParentTest() {
    GitPath a = gfs.getPath("/a");
    GitPath b = gfs.getPath("/a/b");
    GitPath bParent = b.getParent();
    assertEquals(a, bParent);
  }

  @Test
  public void rootGetParentTest() {
    GitPath root = gfs.getPath("/");
    assertNull(root.getParent());
  }

  @Test
  public void relativePathGetParentTest() {
    GitPath a = gfs.getPath("a");
    GitPath b = gfs.getPath("a/b");
    GitPath bParent = b.getParent();
    assertEquals(a, bParent);
  }

  @Test
  public void singleNameRelativePathGetParentTest() {
    GitPath a = gfs.getPath("a");
    assertNull(a.getParent());
  }

  @Test
  public void emptyPathGetParentTest() {
    GitPath empty = gfs.getPath("");
    assertNull(empty.getParent());
  }

}
