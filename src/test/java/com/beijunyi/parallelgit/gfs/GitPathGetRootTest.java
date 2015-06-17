package com.beijunyi.parallelgit.gfs;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathGetRootTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathGetRootTest() {
    GitPath a = gfs.getPath("/a");
    Assert.assertEquals(gfs.getRoot(), a.getRoot());
  }

  @Test
  public void relativePathGetRootTest() {
    GitPath a = gfs.getPath("/a");
    Assert.assertEquals(gfs.getRoot(), a.getRoot());
  }

  @Test
  public void rootPathGetRootTest() {
    GitPath a = gfs.getPath("/");
    Assert.assertEquals(gfs.getRoot(), a.getRoot());
  }

  @Test
  public void emptyPathGetRootTest() {
    GitPath a = gfs.getPath("");
    Assert.assertEquals(gfs.getRoot(), a.getRoot());
  }


}
