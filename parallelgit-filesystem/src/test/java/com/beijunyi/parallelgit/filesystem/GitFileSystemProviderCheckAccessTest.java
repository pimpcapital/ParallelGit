package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.NoSuchFileException;

import org.junit.Before;
import org.junit.Test;

import static org.eclipse.jgit.lib.FileMode.EXECUTABLE_FILE;

public class GitFileSystemProviderCheckAccessTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initRepository();
    writeToCache("dir/file.txt");
    writeToCache("dir/executable.sh", someBytes(), EXECUTABLE_FILE);
    commitToMaster();
    initGitFileSystem();
  }

  @Test
  public void fileCheckReadAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/dir/file.txt"), AccessMode.READ);
  }

  @Test
  public void fileCheckWriteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/dir/file.txt"), AccessMode.WRITE);
  }

  @Test
  public void fileCheckReadWriteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/dir/file.txt"), AccessMode.READ, AccessMode.WRITE);
  }

  @Test(expected = AccessDeniedException.class)
  public void fileCheckExecuteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/dir/file.txt"), AccessMode.EXECUTE);
  }

  @Test
  public void executableFileCheckExecuteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/dir/executable.sh"), AccessMode.EXECUTE);
  }

  @Test
  public void directoryCheckReadAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/dir"), AccessMode.READ);
  }

  @Test
  public void directoryCheckWriteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/dir"), AccessMode.WRITE);
  }

  @Test
  public void directoryCheckReadWriteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/dir"), AccessMode.READ, AccessMode.WRITE);
  }

  @Test(expected = AccessDeniedException.class)
  public void directoryCheckExecuteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/dir"), AccessMode.EXECUTE);
  }

  @Test
  public void rootCheckReadAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/"), AccessMode.READ);
  }

  @Test
  public void rootCheckWriteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/"), AccessMode.WRITE);
  }

  @Test
  public void rootCheckReadWriteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/"), AccessMode.READ, AccessMode.WRITE);
  }

  @Test(expected = AccessDeniedException.class)
  public void rootCheckExecuteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/"), AccessMode.EXECUTE);
  }

  @Test(expected = NoSuchFileException.class)
  public void nonExistentFileCheckReadAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/non_existent_file.txt"), AccessMode.READ);
  }

  @Test(expected = NoSuchFileException.class)
  public void nonExistentFileCheckWriteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/non_existent_file.txt"), AccessMode.WRITE);
  }

  @Test(expected = NoSuchFileException.class)
  public void nonExistentFileCheckReadWriteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/non_existent_file.txt"), AccessMode.READ, AccessMode.WRITE);
  }

  @Test(expected = NoSuchFileException.class)
  public void nonExistentFileCheckExecuteAccess() throws IOException {
    provider.checkAccess(gfs.getPath("/non_existent_file.txt"), AccessMode.EXECUTE);
  }

}
