package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class FilesDirectoryStreamTest extends AbstractGitFileSystemTest {

 @Test
 public void openDirectory_shouldReturnDirectoryStream() throws IOException {
   initRepository();
   writeToCache("/dir/file.txt");
   commitToMaster();
   initGitFileSystem();
   assertNotNull(Files.newDirectoryStream(gfs.getPath("/dir")));
 }

  @Test(expected = NotDirectoryException.class)
  public void openRegularFile_shouldThrowException() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();
    Files.newDirectoryStream(gfs.getPath("/file.txt"));
  }

  @Test(expected = NotDirectoryException.class)
  public void openNonExistentDirectory_shouldThrowException() throws IOException {
    initGitFileSystem();
    Files.newDirectoryStream(gfs.getPath("/non_existent_directory"));
  }

}
