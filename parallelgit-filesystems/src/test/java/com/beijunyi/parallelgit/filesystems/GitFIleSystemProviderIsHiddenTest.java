package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFIleSystemProviderIsHiddenTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void rootIsHiddenTest() throws IOException {
    Assert.assertFalse(Files.isHidden(gfs.getRoot()));
  }

  @Test
  public void commonFilePathIsHiddenTest() throws IOException {
    Assert.assertFalse(Files.isHidden(gfs.getPath("file.txt")));
    Assert.assertFalse(Files.isHidden(gfs.getPath("/file.txt")));
  }

  @Test
  public void pathWithNoExtensionIsHiddenTest() throws IOException {
    Assert.assertFalse(Files.isHidden(gfs.getPath("file")));
    Assert.assertFalse(Files.isHidden(gfs.getPath("/file")));
  }

  @Test
  public void pathWithDotIsHiddenTest() throws IOException {
    Assert.assertFalse(Files.isHidden(gfs.getPath(".")));
    Assert.assertFalse(Files.isHidden(gfs.getPath("/.")));
    Assert.assertFalse(Files.isHidden(gfs.getPath("dir/.")));
    Assert.assertFalse(Files.isHidden(gfs.getPath("/dir/.")));
  }

  @Test
  public void pathWithDoubleDotsIsHiddenTest() throws IOException {
    Assert.assertFalse(Files.isHidden(gfs.getPath("..")));
    Assert.assertFalse(Files.isHidden(gfs.getPath("/..")));
    Assert.assertFalse(Files.isHidden(gfs.getPath("dir/..")));
    Assert.assertFalse(Files.isHidden(gfs.getPath("/dir/..")));
  }

  @Test
  public void pathWithFilenameStartingWithDotIsHiddenTest() throws IOException {
    Assert.assertTrue(Files.isHidden(gfs.getPath(".file")));
    Assert.assertTrue(Files.isHidden(gfs.getPath("/.file")));
    Assert.assertTrue(Files.isHidden(gfs.getPath(".file.txt")));
    Assert.assertTrue(Files.isHidden(gfs.getPath("/.file.txt")));
    Assert.assertTrue(Files.isHidden(gfs.getPath("dir/.file.txt")));
    Assert.assertTrue(Files.isHidden(gfs.getPath("/dir/.file.txt")));
    Assert.assertTrue(Files.isHidden(gfs.getPath("dir/../.file.txt")));
    Assert.assertTrue(Files.isHidden(gfs.getPath("/dir/../.file.txt")));
  }
}
