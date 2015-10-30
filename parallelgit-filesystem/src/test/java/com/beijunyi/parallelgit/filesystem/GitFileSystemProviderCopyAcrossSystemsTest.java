package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileSystemProviderCopyAcrossSystemsTest extends AbstractGitFileSystemTest {

  private Repository targetRepo;
  private GitFileSystem targetGfs;

  @Before
  public void setupTargetSystem() throws IOException {
    targetRepo = new TestRepository();
    targetGfs = GitFileSystemBuilder.prepare().provider(provider).repository(targetRepo).build();
  }

  @After
  public void closeTargetSystem() throws IOException {
    targetGfs.close();
    targetRepo.close();
  }

  @Test
  public void copyFileToAnotherSystem_theTargetFileShouldExist() throws IOException {
    initRepository();
    writeToCache("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = targetGfs.getPath("/target.txt");
    provider.copy(source, target);
    assertTrue(Files.exists(target));
  }

  @Test
  public void copyFileToAnotherSystem_theTargetFileShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeToCache("/source.txt", expectedData);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = targetGfs.getPath("/target.txt");
    provider.copy(source, target);
    assertArrayEquals(expectedData, Files.readAllBytes(target));
  }

  @Test
  public void copyDirectoryToAnotherSystem_theTargetDirectoryShouldExist() throws IOException {
    initRepository();
    writeToCache("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    provider.copy(source, target);
    assertTrue(Files.exists(target));
  }

  @Test
  public void copyDirectoryToAnotherSystem_theTargetDirectoryShouldHaveTheSameChildren() throws IOException {
    initRepository();
    writeToCache("/source/file1.txt");
    writeToCache("/source/file2.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    provider.copy(source, target);
    assertTrue(Files.exists(target.resolve("file1.txt")));
    assertTrue(Files.exists(target.resolve("file2.txt")));
  }

  @Test
  public void copyDirectoryToAnotherSystem_theChildrenInTheTargetDirectoryShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData1 = "expected data 1".getBytes();
    writeToCache("/source/file1.txt", expectedData1);
    byte[] expectedData2 = "expected data 2".getBytes();
    writeToCache("/source/file2.txt", expectedData2);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    provider.copy(source, target);
    assertArrayEquals(expectedData1, Files.readAllBytes(target.resolve("file1.txt")));
    assertArrayEquals(expectedData2, Files.readAllBytes(target.resolve("file2.txt")));
  }

}
