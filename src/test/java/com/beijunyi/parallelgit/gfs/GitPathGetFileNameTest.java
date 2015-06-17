package com.beijunyi.parallelgit.gfs;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathGetFileNameTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathGetFileName() {
    GitPath path = gfs.getPath("/a/b");
    GitPath result = path.getFileName();
    Assert.assertNotNull(result);
    Assert.assertEquals("b", result.toString());
  }

  @Test
  public void absolutePathWithSingleNameGetFileName() {
    GitPath path = gfs.getPath("/a");
    GitPath result = path.getFileName();
    Assert.assertNotNull(result);
    Assert.assertEquals("a", result.toString());
  }

  @Test
  public void rootPathGetFileName() {
    GitPath path = gfs.getPath("/");
    GitPath result = path.getFileName();
    Assert.assertNull(result);
  }

  @Test
  public void relativePathGetFileName() {
    GitPath path = gfs.getPath("a/b");
    GitPath result = path.getFileName();
    Assert.assertNotNull(result);
    Assert.assertEquals("b", result.toString());
  }

  @Test
  public void relativePathWithSingleNameGetFileName() {
    GitPath path = gfs.getPath("a");
    GitPath result = path.getFileName();
    Assert.assertNotNull(result);
    Assert.assertEquals("a", result.toString());
  }

  @Test
  public void emptyPathGetFileName() {
    GitPath path = gfs.getPath("");
    GitPath result = path.getFileName();
    Assert.assertNull(result);
  }

  @Test
  public void dotPathGetFileName() {
    GitPath path = gfs.getPath(".");
    GitPath result = path.getFileName();
    Assert.assertNotNull(result);
    Assert.assertEquals(".", result.toString());
  }

  @Test
  public void doubleDotsPathGetFileName() {
    GitPath path = gfs.getPath("..");
    GitPath result = path.getFileName();
    Assert.assertNotNull(result);
    Assert.assertEquals("..", result.toString());
  }

}
