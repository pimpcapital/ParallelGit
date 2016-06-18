package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FilesMoveAcrossSystemsTest extends AbstractGitFileSystemTest {

  private Repository targetRepo;
  private GitFileSystem targetGfs;

  @Before
  public void setupTargetSystem() throws IOException {
    targetRepo = new TestRepository();
    targetGfs = Gfs.newFileSystem(targetRepo);
  }

  @After
  public void closeTargetSystem() throws IOException {
    targetGfs.close();
    targetRepo.close();
  }

  @Test
  public void moveFileToAnotherSystem_theTargetFileShouldExist() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = targetGfs.getPath("/target.txt");
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
    GitPath target = targetGfs.getPath("/target.txt");
    Files.move(source, target);
    assertFalse(Files.exists(source));
  }

  @Test
  public void moveFileToAnotherSystem_theTargetFileShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expected = someBytes();
    writeToCache("/source.txt", expected);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = targetGfs.getPath("/target.txt");
    Files.move(source, target);
    assertArrayEquals(expected, Files.readAllBytes(target));
  }

  @Test
  public void moveDirectoryToAnotherSystem_theTargetDirectoryShouldExist() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    Files.move(source, target);
    assertTrue(Files.exists(target));
  }

  @Test
  public void moveDirectoryToAnotherSystem_theSourceDirectoryShouldNotExist() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    Files.move(source, target);
    assertFalse(Files.exists(source));
  }

  @Test
  public void moveDirectoryToAnotherSystem_theTargetDirectoryShouldHaveTheSameChildren() throws IOException {
    initRepository();
    writeToCache("/source/file1.txt");
    writeToCache("/source/file2.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    Files.move(source, target);
    assertTrue(Files.exists(target.resolve("file1.txt")));
    assertTrue(Files.exists(target.resolve("file2.txt")));
  }

  @Test
  public void moveDirectoryToAnotherSystem_theChildrenInTheTargetDirectoryShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData1 = someBytes();
    writeToCache("/source/file1.txt", expectedData1);
    byte[] expectedData2 = someBytes();
    writeToCache("/source/file2.txt", expectedData2);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    Files.move(source, target);
    assertArrayEquals(expectedData1, Files.readAllBytes(target.resolve("file1.txt")));
    assertArrayEquals(expectedData2, Files.readAllBytes(target.resolve("file2.txt")));
  }

}
