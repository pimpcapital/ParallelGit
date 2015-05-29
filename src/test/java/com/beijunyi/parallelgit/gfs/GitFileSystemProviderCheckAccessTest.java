package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileSystemProviderCheckAccessTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initRepository();
    writeFile("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
    preventDestroyFileSystem();
  }

  @Test
  public void fileIsWritableTest() {
    Assert.assertTrue(Files.isWritable(gfs.getPath("/dir/file.txt")));
  }

  @Test
  public void fileIsReadableTest() {
    Assert.assertTrue(Files.isReadable(gfs.getPath("/dir/file.txt")));
  }

  @Test
  public void fileIsExecutableTest() {
    Assert.assertFalse(Files.isExecutable(gfs.getPath("/dir/file.txt")));
  }

  @Test
  public void directoryIsWritableTest() {
    Assert.assertTrue(Files.isWritable(gfs.getPath("/dir")));
  }

  @Test
  public void directoryIsReadableTest() {
    Assert.assertTrue(Files.isReadable(gfs.getPath("/dir")));
  }

  @Test
  public void directoryIsExecutableTest() {
    Assert.assertFalse(Files.isExecutable(gfs.getPath("/dir")));
  }

  @Test
  public void rootIsWritableTest() {
    Assert.assertTrue(Files.isWritable(gfs.getPath("/")));
  }

  @Test
  public void rootIsReadableTest() {
    Assert.assertTrue(Files.isReadable(gfs.getPath("/")));
  }

  @Test
  public void rootIsExecutableTest() {
    Assert.assertFalse(Files.isExecutable(gfs.getPath("/")));
  }

  @Test
  public void nonExistentFileIsWritableTest() {
    Assert.assertFalse(Files.isWritable(gfs.getPath("/non_existent")));
  }

  @Test
  public void nonExistentFileIsReadableTest() {
    Assert.assertFalse(Files.isReadable(gfs.getPath("/non_existent")));
  }

  @Test
  public void nonExistentFileIsExecutableTest() {
    Assert.assertFalse(Files.isExecutable(gfs.getPath("/non_existent")));
  }





}
