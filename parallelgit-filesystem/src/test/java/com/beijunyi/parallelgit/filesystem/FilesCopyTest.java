package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.junit.Before;
import org.junit.Test;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.*;

public class FilesCopyTest extends AbstractGitFileSystemTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void copyFile_theTargetFileShouldBeIdenticalToTheSourceFile() throws IOException {
    byte[] expected = someBytes();
    writeToCache("/source.txt", expected);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.copy(source, target);
    assertTrue(Files.exists(target));
    assertTrue(Files.isRegularFile(target));
    assertArrayEquals(expected, Files.readAllBytes(target));
  }

  @Test
  public void copyFile_theSourceFileDataShouldRemainTheSame() throws IOException {
    byte[] expected = someBytes();
    writeToCache("/source.txt", expected);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.copy(source, target);
    assertArrayEquals(expected, Files.readAllBytes(source));
  }

  @Test
  public void copyFile_theFileSystemShouldBecomeDirty() throws IOException {
    writeToCache("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.copy(source, target);
    assertTrue(gfs.getStatusProvider().isDirty());
  }

  @Test
  public void copyModifiedFile_theTargetFileShouldHaveTheModifiedContent() throws IOException {
    writeToCache("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    byte[] expected = someBytes();
    Files.write(source, expected);
    GitPath target = gfs.getPath("/target.txt");
    Files.copy(source, target);
    assertArrayEquals(expected, Files.readAllBytes(target));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void copyFileWhenTargetExists_shouldThrowFileAlreadyExistsException() throws IOException {
    writeToCache("/source.txt");
    writeToCache("/target.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.copy(source, target);
  }

  @Test
  public void copyFileWithReplaceExistingOption_shouldOverwriteTheTargetFileData() throws IOException {
    byte[] expected = someBytes();
    writeToCache("/source.txt", expected);
    writeToCache("/target.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.copy(source, target, REPLACE_EXISTING);
    assertArrayEquals(expected, Files.readAllBytes(target));
  }

  @Test
  public void copyFileWhenTargetEqualsSource_shouldHaveNoEffect() throws IOException {
    byte[] expected = someBytes();
    writeToCache("/source.txt", expected);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    Files.copy(source, source);
    assertArrayEquals(expected, Files.readAllBytes(source));
  }

  @Test(expected = NoSuchFileException.class)
  public void copyNonExistentFile_shouldThrowNoSuchFileException() throws IOException {
    initGitFileSystem();
    GitPath source = gfs.getPath("/non_existent_file.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.copy(source, target);
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void copyFileWhenTargetIsDirectory_shouldThrowFileAlreadyExistsException() throws IOException {
    writeToCache("/source.txt");
    writeToCache("/target/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target");
    Files.copy(source, target);
  }

  @Test
  public void copyFileReplacingTargetDirectory_theTargetShouldBecomeFile() throws IOException {
    writeToCache("/source.txt");
    writeToCache("/target/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target");
    Files.copy(source, target, REPLACE_EXISTING);
    assertTrue(Files.isRegularFile(target));
  }

  @Test
  public void copyFileReplacingTargetDirectory_theTargetFileShouldHaveTheSameData() throws IOException {
    byte[] expected = someBytes();
    writeToCache("/source.txt", expected);
    writeToCache("/target/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target");
    Files.copy(source, target, REPLACE_EXISTING);
    assertArrayEquals(expected, Files.readAllBytes(target));
  }

  @Test
  public void copyDirectory_theTargetDirectoryShouldExist() throws IOException {
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    Files.copy(source, target);
    assertTrue(Files.exists(target));
  }

  @Test
  public void copyDirectory_theTargetDirectoryShouldBeDirectory() throws IOException {
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    Files.copy(source, target);
    assertTrue(Files.isDirectory(target));
  }

  @Test
  public void copyDirectory_theTargetDirectoryShouldHaveTheSameChildren() throws IOException {
    writeToCache("/source/file1.txt");
    writeToCache("/source/file2.txt");
    commitToMaster();
    initGitFileSystem();

    Files.copy(gfs.getPath("/source"), gfs.getPath("/target"));
    assertTrue(Files.exists(gfs.getPath("/target/file1.txt")));
    assertTrue(Files.exists(gfs.getPath("/target/file2.txt")));
  }

  @Test
  public void copyDirectory_theSourceDirectoryShouldRemainTheSame() throws IOException {
    writeToCache("/source/file1.txt");
    writeToCache("/source/file2.txt");
    commitToMaster();
    initGitFileSystem();

    Files.copy(gfs.getPath("/source"), gfs.getPath("/target"));
    assertTrue(Files.exists(gfs.getPath("/source/file1.txt")));
    assertTrue(Files.exists(gfs.getPath("/source/file2.txt")));
  }

  @Test
  public void copyDirectory_theChildrenInTheTargetDirectoryShouldHaveTheSameData() throws IOException {
    byte[] expectedData1 = someBytes();
    writeToCache("/source/file1.txt", expectedData1);
    byte[] expectedData2 = someBytes();
    writeToCache("/source/file2.txt", expectedData2);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    Files.copy(source, target);
    assertArrayEquals(expectedData1, Files.readAllBytes(target.resolve("file1.txt")));
    assertArrayEquals(expectedData2, Files.readAllBytes(target.resolve("file2.txt")));
  }

  @Test
  public void copyDirectory_theFileSystemShouldBecomeDirty() throws IOException {
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    Files.copy(source, target);
    assertTrue(gfs.getStatusProvider().isDirty());
  }

}
