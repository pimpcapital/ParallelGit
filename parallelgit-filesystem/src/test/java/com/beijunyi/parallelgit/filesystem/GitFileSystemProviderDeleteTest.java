package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderDeleteTest extends AbstractGitFileSystemTest {

  @Test
  public void deleteFile_fileShouldNotExistAfterDeletion() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath path = gfs.getPath("/file.txt");
    provider.delete(path);
    Assert.assertFalse(Files.exists(path));
  }

  @Test
  public void deleteEmptyDirectory_directoryShouldNotExistAfterDeletion() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath file = gfs.getPath("/dir/file.txt");
    provider.delete(file);
    GitPath dir = gfs.getPath("/dir");
    provider.delete(dir);
    Assert.assertFalse(Files.exists(dir));
  }

  @Test
  public void deleteNonEmptyDirectory_directoryShouldNotExistAfterDeletion() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath dir = gfs.getPath("/dir");
    provider.delete(dir);
    Assert.assertFalse(Files.exists(dir));
  }

  @Test(expected = NoSuchFileException.class)
  public void deleteNonExistentFile_shouldThrowException() throws IOException {
    initGitFileSystem();
    provider.delete(gfs.getPath("/non_existent_file.txt"));
  }

}
