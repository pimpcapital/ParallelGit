package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;

import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileSystemProviderCopyTest extends AbstractGitFileSystemTest {

  @Test
  public void copyFile_theTargetFileShouldExist() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.copy(source, target);
    assertTrue(Files.exists(target));
  }

  @Test
  public void copyFile_theTargetFileShouldBeFile() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.copy(source, target);
    assertTrue(Files.isRegularFile(target));
  }

  @Test
  public void copyFile_theTargetFileShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeToCache("/source.txt", expectedData);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.copy(source, target);
    assertArrayEquals(expectedData, Files.readAllBytes(target));
  }

  @Test
  public void copyFile_theSourceFileShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeToCache("/source.txt", expectedData);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.copy(source, target);
    assertArrayEquals(expectedData, Files.readAllBytes(source));
  }


  @Test(expected = FileAlreadyExistsException.class)
  public void copyFileWhenTargetExists_shouldThrowFileAlreadyExistsException() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    writeToCache("/target.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.copy(source, target);
  }

  @Test
  public void copyFileWithReplaceExistingOption_shouldOverwriteTheTargetFileData() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeToCache("/source.txt", expectedData);
    writeToCache("/target.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    assertArrayEquals(expectedData, Files.readAllBytes(target));
  }

  @Test
  public void copyFileWhenTargetEqualsSource_shouldHaveNoEffect() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeToCache("/source.txt", expectedData);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    provider.copy(source, source);
    assertArrayEquals(expectedData, Files.readAllBytes(source));
  }

  @Test(expected = NoSuchFileException.class)
  public void copyNonExistentFile_shouldThrowNoSuchFileException() throws IOException {
    initGitFileSystem();
    GitPath source = gfs.getPath("/non_existent_file.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.copy(source, target);
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void copyFileWhenTargetIsDirectory_shouldThrowFileAlreadyExistsException() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    writeToCache("/target/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target");
    provider.copy(source, target);
  }

  @Test
  public void copyFileReplacingTargetDirectory_theTargetShouldBecomeFile() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    writeToCache("/target/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target");
    provider.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    assertTrue(Files.isRegularFile(target));
  }

  @Test
  public void copyFileReplacingTargetDirectory_theTargetFileShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeToCache("/source.txt", expectedData);
    writeToCache("/target/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target");
    provider.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    assertArrayEquals(expectedData, Files.readAllBytes(target));
  }

  @Test
  public void copyDirectory_theTargetDirectoryShouldExist() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    provider.copy(source, target);
    assertTrue(Files.exists(target));
  }

  @Test
  public void copyDirectory_theTargetDirectoryShouldBeDirectory() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    provider.copy(source, target);
    assertTrue(Files.isDirectory(target));
  }

  @Test
  public void copyDirectory_theTargetDirectoryShouldHaveTheSameChildren() throws IOException {
    initRepository();
    writeToCache("/source/file1.txt");
    writeToCache("/source/file2.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    provider.copy(source, target);
    assertTrue(Files.exists(target.resolve("file1.txt")));
    assertTrue(Files.exists(target.resolve("file2.txt")));
  }

  @Test
  public void copyDirectory_theSourceDirectoryShouldHaveTheSameChildren() throws IOException {
    initRepository();
    writeToCache("/source/file1.txt");
    writeToCache("/source/file2.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    provider.copy(source, target);
    assertTrue(Files.exists(source.resolve("file1.txt")));
    assertTrue(Files.exists(source.resolve("file2.txt")));
  }

  @Test
  public void copyDirectory_theChildrenInTheTargetDirectoryShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData1 = "expected data 1".getBytes();
    writeToCache("/source/file1.txt", expectedData1);
    byte[] expectedData2 = "expected data 2".getBytes();
    writeToCache("/source/file2.txt", expectedData2);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    provider.copy(source, target);
    assertArrayEquals(expectedData1, Files.readAllBytes(target.resolve("file1.txt")));
    assertArrayEquals(expectedData2, Files.readAllBytes(target.resolve("file2.txt")));
  }

}
