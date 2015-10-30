package com.beijunyi.parallelgit.filesystem;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitPathToRealPathTest extends PreSetupGitFileSystemTest {

  @Test
  public void rootPathToRealPathTest() {
    assertEquals(root, root.toRealPath());
  }

  @Test
  public void emptyPathToRealPathTest() {
    assertEquals(root, gfs.getPath("").toRealPath());
  }

  @Test
  public void commonAbsoluteFilePathToRealPathTest() {
    assertEquals(gfs.getPath("/file.txt"), gfs.getPath("/file.txt").toRealPath());
  }

  @Test
  public void commonRelativeFilePathToRealPathTest() {
    assertEquals(gfs.getPath("/file.txt"), gfs.getPath("file.txt").toRealPath());
  }

  @Test
  public void absolutePathWithDotToRealPathTest() {
    assertEquals(gfs.getPath("/file.txt"), gfs.getPath("/./file.txt").toRealPath());
    assertEquals(gfs.getPath("/dir/file.txt"), gfs.getPath("/dir/./file.txt").toRealPath());
  }

  @Test
  public void relativePathWithDotToRealPathTest() {
    assertEquals(gfs.getPath("/file.txt"), gfs.getPath("./file.txt").toRealPath());
    assertEquals(gfs.getPath("/dir/file.txt"), gfs.getPath("dir/./file.txt").toRealPath());
  }

  @Test
  public void absolutePathWithDoubleDotsToRealPathTest() {
    assertEquals(gfs.getPath("/file.txt"), gfs.getPath("/dir/../file.txt").toRealPath());
  }

  @Test
  public void relativePathWithDoubleDotsToRealPathTest() {
    assertEquals(gfs.getPath("/file.txt"), gfs.getPath("dir/../file.txt").toRealPath());
  }
}
