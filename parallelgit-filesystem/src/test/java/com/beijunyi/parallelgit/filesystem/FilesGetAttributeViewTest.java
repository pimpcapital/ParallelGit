package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import javax.annotation.Nonnull;

import org.junit.Test;

import static org.junit.Assert.*;

public class FilesGetAttributeViewTest extends AbstractGitFileSystemTest {

  @Test
  public void getFileAttributeViewFromFile_shouldBeNotNull() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();
    assertNotNull(Files.getFileAttributeView(gfs.getPath("/file.txt"), FileAttributeView.class));
  }

  @Test
  public void getFileAttributeViewFromNonExistentFile_shouldBeNotNull() throws IOException {
    initGitFileSystem();
    assertNull(Files.getFileAttributeView(gfs.getPath("/non_existent_file.txt"), FileAttributeView.class));
  }

  @Test
  public void getBasicFileAttributeViewFromFile_shouldBeNotNull() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();
    assertNotNull(Files.getFileAttributeView(gfs.getPath("/file.txt"), BasicFileAttributeView.class));
  }

  @Test
  public void getPosixFileAttributeViewFromFile_shouldBeNotNull() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();
    assertNotNull(Files.getFileAttributeView(gfs.getPath("/file.txt"), PosixFileAttributeView.class));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUnsupportedFileAttributeViewFromFile_shouldThrowUnsupportedOperationException() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();
    assertNotNull(Files.getFileAttributeView(gfs.getPath("/file.txt"), new FileAttributeView() {
      @Nonnull
      @Override
      public String name() {
        return "some_unsupported_view";
      }
    }.getClass()));
  }

}
