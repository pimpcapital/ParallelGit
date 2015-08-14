package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderGetAttributeViewTest extends AbstractGitFileSystemTest {

  @Test
  public void getFileAttributeViewFromFile_shouldBeNotNull() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();
    Assert.assertNotNull(provider.getFileAttributeView(gfs.getPath("/file.txt"), FileAttributeView.class));
  }

  @Test
  public void getFileAttributeViewFromNonExistentFile_shouldBeNotNull() throws IOException {
    initGitFileSystem();
    Assert.assertNull(provider.getFileAttributeView(gfs.getPath("/non_existent_file.txt"), FileAttributeView.class));
  }

  @Test
  public void getBasicFileAttributeViewFromFile_shouldBeNotNull() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();
    Assert.assertNotNull(provider.getFileAttributeView(gfs.getPath("/file.txt"), BasicFileAttributeView.class));
  }

  @Test
  public void getPosixFileAttributeViewFromFile_shouldBeNotNull() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();
    Assert.assertNotNull(provider.getFileAttributeView(gfs.getPath("/file.txt"), PosixFileAttributeView.class));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUnsupportedFileAttributeViewFromFile_shouldThrowUnsupportedOperationException() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();
    Assert.assertNotNull(provider.getFileAttributeView(gfs.getPath("/file.txt"), new FileAttributeView() {
      @Nonnull
      @Override
      public String name() {
        return "some_unsupported_view";
      }
    }.getClass()));
  }

}
