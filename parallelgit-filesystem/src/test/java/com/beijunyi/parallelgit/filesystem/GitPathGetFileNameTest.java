package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitPathGetFileNameTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathGetFileName() {
    GitPath path = gfs.getPath("/a/b");
    GitPath result = path.getFileName();
    assertNotNull(result);
    assertEquals("b", result.toString());
  }

  @Test
  public void absolutePathWithSingleNameGetFileName() {
    GitPath path = gfs.getPath("/a");
    GitPath result = path.getFileName();
    assertNotNull(result);
    assertEquals("a", result.toString());
  }

  @Test
  public void rootPathGetFileName() {
    GitPath path = gfs.getPath("/");
    GitPath result = path.getFileName();
    assertNull(result);
  }

  @Test
  public void relativePathGetFileName() {
    GitPath path = gfs.getPath("a/b");
    GitPath result = path.getFileName();
    assertNotNull(result);
    assertEquals("b", result.toString());
  }

  @Test
  public void relativePathWithSingleNameGetFileName() {
    GitPath path = gfs.getPath("a");
    GitPath result = path.getFileName();
    assertNotNull(result);
    assertEquals("a", result.toString());
  }

  @Test
  public void emptyPathGetFileName() {
    GitPath path = gfs.getPath("");
    GitPath result = path.getFileName();
    assertNull(result);
  }

  @Test
  public void dotPathGetFileName() {
    GitPath path = gfs.getPath(".");
    GitPath result = path.getFileName();
    assertNotNull(result);
    assertEquals(".", result.toString());
  }

  @Test
  public void doubleDotsPathGetFileName() {
    GitPath path = gfs.getPath("..");
    GitPath result = path.getFileName();
    assertNotNull(result);
    assertEquals("..", result.toString());
  }

}
