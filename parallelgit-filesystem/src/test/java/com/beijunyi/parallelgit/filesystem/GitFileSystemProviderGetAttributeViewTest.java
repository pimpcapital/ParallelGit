package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import com.sun.nio.zipfs.ZipFileAttributeView;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderGetAttributeViewTest extends AbstractGitFileSystemTest {

  @Test
  public void getGitFileAttributeViewNameTest() throws IOException {
    initGitFileSystem();
    Assert.assertEquals("basic", Files.getFileAttributeView(root, GitFileAttributeView.class).name());
  }

  @Test
  public void getFileAttributeViewForFileTest() throws IOException {
    initRepository();
    writeFile("a/b");
    commitToMaster();
    initGitFileSystem();
    Assert.assertNotNull(Files.getFileAttributeView(gfs.getPath("/a/b"), GitFileAttributeView.class));
  }

  @Test
  public void getFileAttributeViewForDirectoryTest() throws IOException {
    initRepository();
    writeFile("a/b");
    commitToMaster();
    initGitFileSystem();
    Assert.assertNotNull(Files.getFileAttributeView(gfs.getPath("/a"), GitFileAttributeView.class));
  }

  @Test
  public void getFileAttributeViewForNonExistingFileTest() throws IOException {
    initGitFileSystem();
    Assert.assertNotNull(provider.getFileAttributeView(gfs.getPath("/a"), GitFileAttributeView.class));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getNonGitFileAttributeViewTest() throws IOException {
    initGitFileSystem();
    Files.getFileAttributeView(root, ZipFileAttributeView.class);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void gitFileAttributeViewSetTimesTest() throws IOException {
    initGitFileSystem();
    FileTime now = FileTime.fromMillis(System.currentTimeMillis());
    Files.getFileAttributeView(root, GitFileAttributeView.Basic.class).setTimes(now, now, now);
  }

}
