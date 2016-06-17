package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.Test;

import static java.nio.file.Files.*;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertTrue;

public class FilesCreateFileTest extends PreSetupGitFileSystemTest {

  @Test
  public void createFile_fileShouldBeCreated() throws IOException {
    Path newFile = gfs.getPath("/new_file.txt");
    createFile(newFile);
    assertTrue(exists(newFile));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createFileWhenFileAlreadyExists_shouldThrowFileAlreadyExistsException() throws IOException {
    Path newFile = gfs.getPath("/new_file.txt");
    createFile(newFile);
    createFile(newFile);
  }

  @Test
  public void createFileWithExecutableOption_fileShouldBeExcutable() throws IOException {
    Path newFile = gfs.getPath("/new_file.txt");
    createFile(newFile, PosixFilePermissions.asFileAttribute(singleton(OWNER_EXECUTE)));
    Files.isExecutable(newFile);
  }

}
