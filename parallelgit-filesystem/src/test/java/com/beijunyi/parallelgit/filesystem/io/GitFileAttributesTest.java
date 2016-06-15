package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static java.nio.file.Files.*;
import static org.eclipse.jgit.lib.FileMode.EXECUTABLE_FILE;
import static org.junit.Assert.*;

public class GitFileAttributesTest extends AbstractGitFileSystemTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void getObjectId_shouldReturnTheTheIdOfTheNode() throws IOException {
    ObjectId blobId = writeToCache("/test_file.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileAttributes attributes = readGitAttributes("/test_file.txt");
    assertEquals(blobId, attributes.getObjectId());
  }

  @Test
  public void getMode_shouldReturnTheModeOfTheNode() throws IOException {
    writeToCache("/test_file.txt", someBytes(), EXECUTABLE_FILE);
    commitToMaster();
    initGitFileSystem();

    GitFileAttributes attributes = readGitAttributes("/test_file.txt");
    assertEquals(EXECUTABLE_FILE, attributes.getFileMode());
  }

  @Test
  public void testIsNewOnNewFile_shouldReturnTrue() throws IOException {
    initGitFileSystem();
    writeToGfs("/test_file.txt");

    GitFileAttributes attributes = readGitAttributes("/test_file.txt");
    assertTrue(attributes.isNew());
  }

  @Test
  public void testIsNewOnExistingFile_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileAttributes attributes = readGitAttributes("/test_file.txt");
    assertFalse(attributes.isNew());
  }

  @Test
  public void testIsModifiedOnUnchangedFile_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileAttributes attributes = readGitAttributes("/test_file.txt");
    assertFalse(attributes.isModified());
  }

  @Test
  public void testIsModifiedOnChangedFile_shouldReturnTrue() throws IOException {
    initGitFileSystem();
    writeToGfs("/test_file.txt");

    write(gfs.getPath("/test_file.txt"), someBytes());
    GitFileAttributes attributes = readGitAttributes("/test_file.txt");
    assertTrue(attributes.isModified());
  }

  @Test
  public void testIsModifiedOnNewFile_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    commitToMaster();
    initGitFileSystem();

    write(gfs.getPath("/test_file.txt"), someBytes());
    GitFileAttributes attributes = readGitAttributes("/test_file.txt");
    assertTrue(attributes.isModified());
  }

  @Nonnull
  private GitFileAttributes readGitAttributes(String path) throws IOException {
    return readAttributes(gfs.getPath(path), GitFileAttributes.class);
  }

}
