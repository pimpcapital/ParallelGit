package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathToAbsolutePathTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathToAbsolutePathTest() {
    GitPath path = gfs.getPath("/a/b");
    GitPath result = path.toAbsolutePath();
    Assert.assertEquals("/a/b", result.toString());
  }

  @Test
  public void rootPathToAbsolutePathTest() {
    GitPath path = gfs.getPath("/");
    GitPath result = path.toAbsolutePath();
    Assert.assertEquals("/", result.toString());
  }

  @Test
  public void relativePathToAbsolutePathTest() {
    GitPath path = gfs.getPath("a/b");
    GitPath result = path.toAbsolutePath();
    Assert.assertEquals("/a/b", result.toString());
  }

  @Test
  public void emptyPathToAbsolutePathTest() {
    GitPath path = gfs.getPath("");
    GitPath result = path.toAbsolutePath();
    Assert.assertEquals("/", result.toString());
  }
}
