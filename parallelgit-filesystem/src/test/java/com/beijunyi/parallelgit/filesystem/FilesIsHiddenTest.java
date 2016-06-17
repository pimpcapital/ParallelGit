package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FilesIsHiddenTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void rootIsHiddenTest() throws IOException {
    assertFalse(Files.isHidden(gfs.getRootPath()));
  }

  @Test
  public void commonFilePathIsHiddenTest() throws IOException {
    assertFalse(Files.isHidden(gfs.getPath("file.txt")));
    assertFalse(Files.isHidden(gfs.getPath("/file.txt")));
  }

  @Test
  public void pathWithNoExtensionIsHiddenTest() throws IOException {
    assertFalse(Files.isHidden(gfs.getPath("file")));
    assertFalse(Files.isHidden(gfs.getPath("/file")));
  }

  @Test
  public void pathWithDotIsHiddenTest() throws IOException {
    assertFalse(Files.isHidden(gfs.getPath(".")));
    assertFalse(Files.isHidden(gfs.getPath("/.")));
    assertFalse(Files.isHidden(gfs.getPath("dir/.")));
    assertFalse(Files.isHidden(gfs.getPath("/dir/.")));
  }

  @Test
  public void pathWithDoubleDotsIsHiddenTest() throws IOException {
    assertFalse(Files.isHidden(gfs.getPath("..")));
    assertFalse(Files.isHidden(gfs.getPath("/..")));
    assertFalse(Files.isHidden(gfs.getPath("dir/..")));
    assertFalse(Files.isHidden(gfs.getPath("/dir/..")));
  }

  @Test
  public void pathWithFilenameStartingWithDotIsHiddenTest() throws IOException {
    assertTrue(Files.isHidden(gfs.getPath(".file")));
    assertTrue(Files.isHidden(gfs.getPath("/.file")));
    assertTrue(Files.isHidden(gfs.getPath(".file.txt")));
    assertTrue(Files.isHidden(gfs.getPath("/.file.txt")));
    assertTrue(Files.isHidden(gfs.getPath("dir/.file.txt")));
    assertTrue(Files.isHidden(gfs.getPath("/dir/.file.txt")));
    assertTrue(Files.isHidden(gfs.getPath("dir/../.file.txt")));
    assertTrue(Files.isHidden(gfs.getPath("/dir/../.file.txt")));
  }
}
