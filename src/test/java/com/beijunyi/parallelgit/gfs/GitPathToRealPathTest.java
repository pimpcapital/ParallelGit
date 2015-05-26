package com.beijunyi.parallelgit.gfs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathToRealPathTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() {
    initGitFileSystem();
    preventDestroyFileSystem();
  }

  @Test
  public void rootPathToRealPathTest() {
    Assert.assertEquals(root, root.toRealPath());
  }

  @Test
  public void emptyPathToRealPathTest() {
    Assert.assertEquals(root, gfs.getPath("").toRealPath());
  }

  @Test
  public void commonAbsoluteFilePathToRealPathTest() {
    Assert.assertEquals(gfs.getPath("/file.txt"), gfs.getPath("/file.txt").toRealPath());
  }

  @Test
  public void commonRelativeFilePathToRealPathTest() {
    Assert.assertEquals(gfs.getPath("/file.txt"), gfs.getPath("file.txt").toRealPath());
  }

  @Test
  public void absolutePathWithDotToRealPathTest() {
    Assert.assertEquals(gfs.getPath("/file.txt"), gfs.getPath("/./file.txt").toRealPath());
    Assert.assertEquals(gfs.getPath("/dir/file.txt"), gfs.getPath("/dir/./file.txt").toRealPath());
  }

  @Test
  public void relativePathWithDotToRealPathTest() {
    Assert.assertEquals(gfs.getPath("/file.txt"), gfs.getPath("./file.txt").toRealPath());
    Assert.assertEquals(gfs.getPath("/dir/file.txt"), gfs.getPath("dir/./file.txt").toRealPath());
  }

  @Test
  public void absolutePathWithDoubleDotsToRealPathTest() {
    Assert.assertEquals(gfs.getPath("/file.txt"), gfs.getPath("/dir/../file.txt").toRealPath());
  }

  @Test
  public void relativePathWithDoubleDotsToRealPathTest() {
    Assert.assertEquals(gfs.getPath("/file.txt"), gfs.getPath("dir/../file.txt").toRealPath());
  }
}
