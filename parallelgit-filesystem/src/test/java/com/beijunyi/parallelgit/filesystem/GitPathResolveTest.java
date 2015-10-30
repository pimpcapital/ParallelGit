package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitPathResolveTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void resolveFileFromRootTest() {
    GitPath path = root.resolve("a.txt");
    assertEquals("/a.txt", path.toString());
  }

  @Test
  public void resolveFileInDirectoryFromRootTest() {
    GitPath path = root.resolve("a/b/c.txt");
    assertEquals("/a/b/c.txt", path.toString());
  }

  @Test
  public void resolveDirectoryFromRootTest() {
    GitPath path = root.resolve("a/b/c/");
    assertEquals("/a/b/c", path.toString());
  }

  @Test
  public void resolvePathWithDuplicatedSeparatorsFromRootTest() {
    GitPath path = root.resolve("a///b//c.txt");
    assertEquals("/a/b/c.txt", path.toString());
  }

  @Test
  public void resolveAbsolutePathFromRootTest() {
    GitPath path = root.resolve("/a.txt");
    assertEquals("/a.txt", path.toString());
  }

  @Test
  public void resolveRootPathFromRootTest() {
    GitPath path = root.resolve("/");
    assertEquals("/", path.toString());
  }

  @Test
  public void resolveEmptyPathFromRootTest() {
    GitPath path = root.resolve("");
    assertEquals("/", path.toString());
  }

  @Test
  public void resolveFileFromAbsolutePathTest() {
    GitPath parent = gfs.getPath("/parent/dir");
    GitPath path = parent.resolve("a.txt");
    assertEquals("/parent/dir/a.txt", path.toString());
  }

  @Test
  public void resolveFileInDirectoryFromAbsolutePathTest() {
    GitPath parent = gfs.getPath("/parent/dir");
    GitPath path = parent.resolve("a/b/c.txt");
    assertEquals("/parent/dir/a/b/c.txt", path.toString());
  }

  @Test
  public void resolveDirectoryFromAbsolutePathTest() {
    GitPath parent = gfs.getPath("/parent/dir");
    GitPath path = parent.resolve("a/b/c/");
    assertEquals("/parent/dir/a/b/c", path.toString());
  }

  @Test
  public void resolvePathWithDuplicatedSeparatorsFromAbsolutePathTest() {
    GitPath parent = gfs.getPath("/parent/dir");
    GitPath path = parent.resolve("a///b//c.txt");
    assertEquals("/parent/dir/a/b/c.txt", path.toString());
  }

  @Test
  public void resolveAbsolutePathFromAbsolutePathTest() {
    GitPath parent = gfs.getPath("/parent/dir");
    GitPath path = parent.resolve("/a.txt");
    assertEquals("/a.txt", path.toString());
  }

  @Test
  public void resolveRootPathFromAbsolutePathTest() {
    GitPath parent = gfs.getPath("/parent/dir");
    GitPath path = parent.resolve("/");
    assertEquals("/", path.toString());
  }

  @Test
  public void resolveEmptyPathFromAbsolutePathTest() {
    GitPath parent = gfs.getPath("/parent/dir");
    GitPath path = parent.resolve("");
    assertEquals("/parent/dir", path.toString());
  }

  @Test
  public void resolveFileFromRelativePathTest() {
    GitPath parent = gfs.getPath("parent/dir");
    GitPath path = parent.resolve("a.txt");
    assertEquals("parent/dir/a.txt", path.toString());
  }

  @Test
  public void resolveFileInDirectoryFromRelativePathTest() {
    GitPath parent = gfs.getPath("parent/dir");
    GitPath path = parent.resolve("a/b/c.txt");
    assertEquals("parent/dir/a/b/c.txt", path.toString());
  }

  @Test
  public void resolveDirectoryFromRelativePathTest() {
    GitPath parent = gfs.getPath("parent/dir");
    GitPath path = parent.resolve("a/b/c/");
    assertEquals("parent/dir/a/b/c", path.toString());
  }

  @Test
  public void resolvePathWithDuplicatedSeparatorsFromRelativePathTest() {
    GitPath parent = gfs.getPath("parent/dir");
    GitPath path = parent.resolve("a///b//c.txt");
    assertEquals("parent/dir/a/b/c.txt", path.toString());
  }

  @Test
  public void resolveAbsolutePathFromRelativePathTest() {
    GitPath parent = gfs.getPath("parent/dir");
    GitPath path = parent.resolve("/a.txt");
    assertEquals("/a.txt", path.toString());
  }

  @Test
  public void resolveRootPathFromRelativePathTest() {
    GitPath parent = gfs.getPath("parent/dir");
    GitPath path = parent.resolve("/");
    assertEquals("/", path.toString());
  }

  @Test
  public void resolveEmptyPathFromRelativePathTest() {
    GitPath parent = gfs.getPath("parent/dir");
    GitPath path = parent.resolve("");
    assertEquals("parent/dir", path.toString());
  }

}
