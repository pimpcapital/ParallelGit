package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Before;
import org.junit.Test;

import static java.nio.file.Files.readAttributes;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static org.eclipse.jgit.lib.FileMode.EXECUTABLE_FILE;
import static org.junit.Assert.*;

public class PosixFileAttributesTest extends AbstractGitFileSystemTest {
  
  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void getPermissionOfFile_shouldReturnNotNull() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    PosixFileAttributes attributes = readPosixAttributes("/file.txt");
    assertNotNull(attributes.permissions());
  }

  @Test
  public void getPermissionOfFile_shouldContainOwnerRead() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    PosixFileAttributes attributes = readPosixAttributes("/file.txt");
    Collection permissions = (Collection) attributes.permissions();
    assertTrue(permissions.contains(PosixFilePermission.OWNER_READ));
  }

  @Test
  public void getPermissionOfFile_shouldContainOwnerWrite() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    PosixFileAttributes attributes = readPosixAttributes("/file.txt");
    Collection permissions = (Collection) attributes.permissions();
    assertTrue(permissions.contains(PosixFilePermission.OWNER_WRITE));
  }

  @Test
  public void getPermissionOfFile_shouldNotContainOwnerExecute() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    PosixFileAttributes attributes = readPosixAttributes("/file.txt");
    Collection permissions = (Collection) attributes.permissions();
    assertFalse(permissions.contains(OWNER_EXECUTE));
  }

  @Test
  public void getPermissionOfExecutableFile_shouldContainOwnerExecute() throws IOException {
    writeToCache("/file.txt", someBytes(), EXECUTABLE_FILE);
    commitToMaster();
    initGitFileSystem();

    PosixFileAttributes attributes = readPosixAttributes("/file.txt");
    Collection permissions = (Collection) attributes.permissions();
    assertTrue(permissions.contains(OWNER_EXECUTE));
  }

  @Test
  public void getOwnerOfFile_shouldReturnNull() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    PosixFileAttributes attributes = readPosixAttributes("/file.txt");
    assertNull(attributes.owner());
  }

  @Test
  public void getGroupOfFile_shouldReturnNull() throws IOException {
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    PosixFileAttributes attributes = readPosixAttributes("/file.txt");
    assertNull(attributes.group());
  }
  
  @Nonnull
  private PosixFileAttributes readPosixAttributes(String path) throws IOException {
    return readAttributes(gfs.getPath(path), PosixFileAttributes.class);
  }

}
