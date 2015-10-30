package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.NotDirectoryException;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class GitFileSystemProviderDirectoryStreamTest extends AbstractGitFileSystemTest {

 @Test
 public void openDirectory_shouldReturnDirectoryStream() throws IOException {
   initRepository();
   writeToCache("/dir/file.txt");
   commitToMaster();
   initGitFileSystem();
   assertNotNull(provider.newDirectoryStream(gfs.getPath("/dir"), null));
 }

  @Test(expected = NotDirectoryException.class)
  public void openRegularFile_shouldThrowException() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();
    provider.newDirectoryStream(gfs.getPath("/file.txt"), null);
  }

  @Test(expected = NotDirectoryException.class)
  public void openNonExistentDirectory_shouldThrowException() throws IOException {
    initGitFileSystem();
    provider.newDirectoryStream(gfs.getPath("/non_existent_directory"), null);
  }

}
