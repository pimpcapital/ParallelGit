package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Test;

import static org.eclipse.jgit.lib.FileMode.*;
import static org.junit.Assert.*;

public class BasicGfsFileAttributesTest extends AbstractGitFileSystemTest {

  @Test
  public void getSize_shouldReturnTheFileSize() throws IOException {
    initRepository();
    byte[] data = "13 bytes data".getBytes();
    writeToCache("/file.txt", data);
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertEquals(13L, attributes.size());
  }

  @Test
  public void getSizeAttributeOfDirectory_shouldReturnZero() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/dir"), BasicFileAttributes.class);
    assertEquals(0L, attributes.size());
  }

  @Test
  public void getCreationTimeAttributeOfFile_shouldReturnEpoch() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertEquals(FileTime.fromMillis(0), attributes.creationTime());
  }

  @Test
  public void getLastAccessTimeAttributeOfFile_shouldReturnEpoch() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertEquals(FileTime.fromMillis(0), attributes.lastAccessTime());
  }

  @Test
  public void getLastModifiedTimeAttributeOfFile_shouldReturnEpoch() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertEquals(FileTime.fromMillis(0), attributes.lastModifiedTime());
  }

  @Test
  public void getFileKeyAttributeOfFile_shouldReturnNull() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertNull(attributes.fileKey());
  }

  @Test
  public void getIsDirectoryAttributeOfFile_shouldReturnFalse() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertEquals(false, attributes.isDirectory());
  }

  @Test
  public void getIsDirectoryAttributeOfDirectory_shouldReturnTrue() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/dir"), BasicFileAttributes.class);
    assertEquals(true, attributes.isDirectory());
  }

  @Test
  public void getIsRegularFileAttributeOfFile_shouldReturnTrue() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertEquals(true, attributes.isRegularFile());
  }

  @Test
  public void getIsRegularFileAttributeOfExecutableFile_shouldReturnTrue() throws IOException {
    initRepository();
    writeToCache("/file.txt", "some data".getBytes(), EXECUTABLE_FILE);
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertEquals(true, attributes.isRegularFile());
  }

  @Test
  public void getIsRegularFileAttributeOfDirectory_shouldReturnFalse() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/dir"), BasicFileAttributes.class);
    assertEquals(false, attributes.isRegularFile());
  }

  @Test
  public void getIsSymbolicLinkAttributeOfFile_shouldReturnFalse() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertEquals(false, attributes.isSymbolicLink());
  }

  @Test
  public void getIsSymbolicLinkAttributeOfSymbolicLink_shouldReturnTrue() throws IOException {
    initRepository();
    writeToCache("/file.txt", "some link".getBytes(), SYMLINK);
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertEquals(true, attributes.isSymbolicLink());
  }

  @Test
  public void getIsOtherAttributeOfFile_shouldReturnFalse() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/file.txt"), BasicFileAttributes.class);
    assertEquals(false, attributes.isOther());
  }
  
}
