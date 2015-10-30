package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitPathGetRootTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathGetRootTest() {
    GitPath a = gfs.getPath("/a");
    assertEquals(gfs.getRootPath(), a.getRoot());
  }

  @Test
  public void relativePathGetRootTest() {
    GitPath a = gfs.getPath("a");
    assertEquals(gfs.getRootPath(), a.getRoot());
  }

  @Test
  public void rootPathGetRootTest() {
    GitPath a = gfs.getPath("/");
    assertEquals(gfs.getRootPath(), a.getRoot());
  }

  @Test
  public void emptyPathGetRootTest() {
    GitPath a = gfs.getPath("");
    assertEquals(gfs.getRootPath(), a.getRoot());
  }


}
