package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;

import org.junit.Test;

import static org.junit.Assert.*;

public class FilesMoveTest extends AbstractGitFileSystemTest {

  @Test
  public void moveFile_theTargetFileShouldExist() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.move(source, target);
    assertTrue(Files.exists(target));
  }

  @Test
  public void moveFile_theSourceFileShouldNotExist() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.move(source, target);
    assertFalse(Files.exists(source));
  }

  @Test
  public void moveFile_theTargetFileShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData = someBytes();
    writeToCache("/source.txt", expectedData);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.move(source, target);
    assertArrayEquals(expectedData, Files.readAllBytes(target));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void moveFileWhenTargetExists_shouldThrowFileAlreadyExistsException() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    writeToCache("/target.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.move(source, target);
  }

  @Test
  public void moveFileWhenTargetExists_theSourceFileShouldExistAfterTheException() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    writeToCache("/target.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    try {
      Files.move(source, target);
    } catch(FileAlreadyExistsException ignore) {
    }
    assertTrue(Files.exists(source));
  }

  @Test
  public void moveFileWithReplaceExistingOption_shouldOverwriteTheTargetFileData() throws IOException {
    initRepository();
    byte[] expectedData = someBytes();
    writeToCache("/source.txt", expectedData);
    writeToCache("/target.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    assertArrayEquals(expectedData, Files.readAllBytes(target));
  }

  @Test
  public void moveFileWhenTargetEqualsSource_shouldHaveNoEffect() throws IOException {
    initRepository();
    byte[] expectedData = someBytes();
    writeToCache("/source.txt", expectedData);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    Files.move(source, source);
    assertArrayEquals(expectedData, Files.readAllBytes(source));
  }

  @Test(expected = NoSuchFileException.class)
  public void moveNonExistentFile_shouldThrowNoSuchFileException() throws IOException {
    initGitFileSystem();
    GitPath source = gfs.getPath("/non_existent_file.txt");
    GitPath target = gfs.getPath("/target.txt");
    Files.move(source, target);
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void moveFileWhenTargetIsDirectory_shouldThrowFileAlreadyExistsException() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    writeToCache("/target/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target");
    Files.move(source, target);
  }

  @Test
  public void moveFileReplacingTargetDirectory_theTargetShouldBecomeFile() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    writeToCache("/target/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target");
    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    assertTrue(Files.isRegularFile(target));
  }

  @Test
  public void moveFileReplacingTargetDirectory_theTargetFileShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData = someBytes();
    writeToCache("/source.txt", expectedData);
    writeToCache("/target/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target");
    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    assertArrayEquals(expectedData, Files.readAllBytes(target));
  }

  @Test
  public void moveDirectory_theTargetDirectoryShouldExist() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    Files.move(source, target);
    assertTrue(Files.exists(target));
  }

  @Test
  public void moveDirectory_theSourceDirectoryShouldNotExist() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    Files.move(source, target);
    assertFalse(Files.exists(source));
  }

  @Test
  public void moveDirectory_theTargetDirectoryShouldBeDirectory() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    Files.move(source, target);
    assertTrue(Files.isDirectory(target));
  }

  @Test
  public void moveDirectory_theTargetDirectoryShouldHaveTheSameChildren() throws IOException {
    initRepository();
    writeToCache("/source/file1.txt");
    writeToCache("/source/file2.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    Files.move(source, target);
    assertTrue(Files.exists(target.resolve("file1.txt")));
    assertTrue(Files.exists(target.resolve("file2.txt")));
  }

  @Test
  public void moveDirectory_theChildrenInTheTargetDirectoryShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData1 = someBytes();
    writeToCache("/source/file1.txt", expectedData1);
    byte[] expectedData2 = someBytes();
    writeToCache("/source/file2.txt", expectedData2);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    Files.move(source, target);
    assertArrayEquals(expectedData1, Files.readAllBytes(target.resolve("file1.txt")));
    assertArrayEquals(expectedData2, Files.readAllBytes(target.resolve("file2.txt")));
  }

}
