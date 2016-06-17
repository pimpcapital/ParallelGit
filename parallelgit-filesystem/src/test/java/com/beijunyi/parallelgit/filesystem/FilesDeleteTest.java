package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.junit.Test;

import static org.junit.Assert.*;

public class FilesDeleteTest extends AbstractGitFileSystemTest {

  @Test
  public void deleteFile_fileShouldNotExistAfterDeletion() throws IOException {
    initGitFileSystem("/test_file.txt");

    GitPath path = gfs.getPath("/test_file.txt");
    Files.delete(path);
    assertFalse(Files.exists(path));
  }

  @Test
  public void deleteFile_theFileSystemShouldBecomeDirty() throws IOException {
    initGitFileSystem("/test_file.txt");

    GitPath path = gfs.getPath("/test_file.txt");
    Files.delete(path);
    assertTrue(gfs.getStatusProvider().isDirty());
  }

  @Test
  public void deleteEmptyDirectory_directoryShouldNotExistAfterDeletion() throws IOException {
    initGitFileSystem("/dir/some_file.txt");

    GitPath file = gfs.getPath("/dir/some_file.txt");
    Files.delete(file);
    GitPath dir = gfs.getPath("/dir");
    Files.delete(dir);
    assertFalse(Files.exists(dir));
  }

  @Test
  public void createAndDeleteEmptyDirectory_theFileSystemShouldRemainClean() throws IOException {
    initGitFileSystem();
    GitPath dir = gfs.getPath("/empty_dir");
    Files.createDirectory(dir);
    Files.delete(dir);
    assertFalse(gfs.getStatusProvider().isDirty());
  }

  @Test
  public void deleteNonEmptyDirectory_directoryShouldNotExistAfterDeletion() throws IOException {
    initGitFileSystem("/dir/some_file.txt");

    GitPath dir = gfs.getPath("/dir");
    Files.delete(dir);
    assertFalse(Files.exists(dir));
  }

  @Test(expected = NoSuchFileException.class)
  public void deleteNonExistentFile_shouldThrowException() throws IOException {
    initGitFileSystem();
    Files.delete(gfs.getPath("/non_existent_file.txt"));
  }

}
