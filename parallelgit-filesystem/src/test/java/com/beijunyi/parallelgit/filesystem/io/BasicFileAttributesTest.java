package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Before;
import org.junit.Test;

import static java.nio.file.Files.readAttributes;
import static org.eclipse.jgit.lib.Constants.encodeASCII;
import static org.eclipse.jgit.lib.FileMode.*;
import static org.junit.Assert.*;

public class BasicFileAttributesTest extends AbstractGitFileSystemTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void getSize_shouldReturnTheFileSize() throws IOException {
    byte[] data = encodeASCII("13 bytes data");
    writeToCache("/file.txt", data);
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertEquals(13L, attributes.size());
  }

  @Test
  public void getSizeAttributeOfDirectory_shouldReturnZero() throws IOException {
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/dir");
    assertEquals(0L, attributes.size());
  }

  @Test
  public void getCreationTimeAttributeOfFile_shouldReturnEpoch() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertEquals(FileTime.fromMillis(0), attributes.creationTime());
  }

  @Test
  public void getLastAccessTimeAttributeOfFile_shouldReturnEpoch() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertEquals(FileTime.fromMillis(0), attributes.lastAccessTime());
  }

  @Test
  public void getLastModifiedTimeAttributeOfFile_shouldReturnEpoch() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertEquals(FileTime.fromMillis(0), attributes.lastModifiedTime());
  }

  @Test
  public void getFileKeyAttributeOfFile_shouldReturnNull() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertNull(attributes.fileKey());
  }

  @Test
  public void getIsDirectoryAttributeOfFile_shouldReturnFalse() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertEquals(false, attributes.isDirectory());
  }

  @Test
  public void getIsDirectoryAttributeOfDirectory_shouldReturnTrue() throws IOException {
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/dir");
    assertEquals(true, attributes.isDirectory());
  }

  @Test
  public void getIsRegularFileAttributeOfFile_shouldReturnTrue() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertEquals(true, attributes.isRegularFile());
  }

  @Test
  public void getIsRegularFileAttributeOfExecutableFile_shouldReturnTrue() throws IOException {
    writeToCache("/file.txt", someBytes(), EXECUTABLE_FILE);
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertEquals(true, attributes.isRegularFile());
  }

  @Test
  public void getIsRegularFileAttributeOfDirectory_shouldReturnFalse() throws IOException {
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = provider.readAttributes(gfs.getPath("/dir"), BasicFileAttributes.class);
    assertEquals(false, attributes.isRegularFile());
  }

  @Test
  public void getIsSymbolicLinkAttributeOfFile_shouldReturnFalse() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertEquals(false, attributes.isSymbolicLink());
  }

  @Test
  public void getIsSymbolicLinkAttributeOfSymbolicLink_shouldReturnTrue() throws IOException {
    writeToCache("/file.txt", someBytes(), SYMLINK);
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertEquals(true, attributes.isSymbolicLink());
  }

  @Test
  public void getIsOtherAttributeOfFile_shouldReturnFalse() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    BasicFileAttributes attributes = readPosixAttributes("/file.txt");
    assertEquals(false, attributes.isOther());
  }

  @Nonnull
  private BasicFileAttributes readPosixAttributes(String path) throws IOException {
    return readAttributes(gfs.getPath(path), BasicFileAttributes.class);
  }
  
}
