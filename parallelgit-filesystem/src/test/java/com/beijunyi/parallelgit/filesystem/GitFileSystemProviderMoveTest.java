package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;

import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderMoveTest extends AbstractGitFileSystemTest {

  @Test
  public void moveFile_theTargetFileShouldExist() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.move(source, target);
    Assert.assertTrue(Files.exists(target));
  }

  @Test
  public void moveFile_theSourceFileShouldNotExist() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.move(source, target);
    Assert.assertFalse(Files.exists(source));
  }

  @Test
  public void moveFile_theTargetFileShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeToCache("/source.txt", expectedData);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.move(source, target);
    Assert.assertArrayEquals(expectedData, Files.readAllBytes(target));
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
    provider.move(source, target);
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
      provider.move(source, target);
    } catch(FileAlreadyExistsException ignore) {
    }
    Assert.assertTrue(Files.exists(source));
  }

  @Test
  public void moveFileWithReplaceExistingOption_shouldOverwriteTheTargetFileData() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeToCache("/source.txt", expectedData);
    writeToCache("/target.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    Assert.assertArrayEquals(expectedData, Files.readAllBytes(target));
  }

  @Test
  public void moveFileWhenTargetEqualsSource_shouldHaveNoEffect() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeToCache("/source.txt", expectedData);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    provider.move(source, source);
    Assert.assertArrayEquals(expectedData, Files.readAllBytes(source));
  }

  @Test(expected = NoSuchFileException.class)
  public void moveNonExistentFile_shouldThrowNoSuchFileException() throws IOException {
    initGitFileSystem();
    GitPath source = gfs.getPath("/non_existent_file.txt");
    GitPath target = gfs.getPath("/target.txt");
    provider.move(source, target);
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
    provider.move(source, target);
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
    provider.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    Assert.assertTrue(Files.isRegularFile(target));
  }

  @Test
  public void moveFileReplacingTargetDirectory_theTargetFileShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeToCache("/source.txt", expectedData);
    writeToCache("/target/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = gfs.getPath("/target");
    provider.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    Assert.assertArrayEquals(expectedData, Files.readAllBytes(target));
  }

  @Test
  public void moveDirectory_theTargetDirectoryShouldExist() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    provider.move(source, target);
    Assert.assertTrue(Files.exists(target));
  }

  @Test
  public void moveDirectory_theSourceDirectoryShouldNotExist() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    provider.move(source, target);
    Assert.assertFalse(Files.exists(source));
  }

  @Test
  public void moveDirectory_theTargetDirectoryShouldBeDirectory() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    provider.move(source, target);
    Assert.assertTrue(Files.isDirectory(target));
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
    provider.move(source, target);
    Assert.assertTrue(Files.exists(target.resolve("file1.txt")));
    Assert.assertTrue(Files.exists(target.resolve("file2.txt")));
  }

  @Test
  public void moveDirectory_theChildrenInTheTargetDirectoryShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData1 = "expected data 1".getBytes();
    writeToCache("/source/file1.txt", expectedData1);
    byte[] expectedData2 = "expected data 2".getBytes();
    writeToCache("/source/file2.txt", expectedData2);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = gfs.getPath("/target");
    provider.move(source, target);
    Assert.assertArrayEquals(expectedData1, Files.readAllBytes(target.resolve("file1.txt")));
    Assert.assertArrayEquals(expectedData2, Files.readAllBytes(target.resolve("file2.txt")));
  }

}
