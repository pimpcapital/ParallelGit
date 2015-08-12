package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.eclipse.jgit.lib.Repository;
import org.junit.*;

public class GitFileSystemProviderMoveAcrossSystemsTest extends AbstractGitFileSystemTest {

  private Repository targetRepo;
  private GitFileSystem targetGfs;

  @Before
  public void setupTargetSystem() throws IOException {
    targetRepo = new TestRepository(true);
    targetGfs = GitFileSystemBuilder.prepare().provider(provider).repository(targetRepo).build();
  }

  @After
  public void closeTargetSystem() throws IOException {
    targetGfs.close();
    targetRepo.close();
  }

  @Test
  public void moveFileToAnotherSystem_theTargetFileShouldExist() throws IOException {
    initRepository();
    writeFile("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = targetGfs.getPath("/target.txt");
    provider.move(source, target);
    Assert.assertTrue(Files.exists(target));
  }

  @Test
  public void moveFile_theSourceFileShouldNotExist() throws IOException {
    initRepository();
    writeFile("/source.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = targetGfs.getPath("/target.txt");
    provider.move(source, target);
    Assert.assertFalse(Files.exists(source));
  }

  @Test
  public void moveFileToAnotherSystem_theTargetFileShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData = "expected data".getBytes();
    writeFile("/source.txt", expectedData);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source.txt");
    GitPath target = targetGfs.getPath("/target.txt");
    provider.move(source, target);
    Assert.assertArrayEquals(expectedData, Files.readAllBytes(target));
  }

  @Test
  public void moveDirectoryToAnotherSystem_theTargetDirectoryShouldExist() throws IOException {
    initRepository();
    writeFile("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    provider.move(source, target);
    Assert.assertTrue(Files.exists(target));
  }

  @Test
  public void moveDirectoryToAnotherSystem_theSourceDirectoryShouldNotExist() throws IOException {
    initRepository();
    writeFile("/source/file.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    provider.move(source, target);
    Assert.assertFalse(Files.exists(source));
  }

  @Test
  public void moveDirectoryToAnotherSystem_theTargetDirectoryShouldHaveTheSameChildren() throws IOException {
    initRepository();
    writeFile("/source/file1.txt");
    writeFile("/source/file2.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    provider.move(source, target);
    Assert.assertTrue(Files.exists(target.resolve("file1.txt")));
    Assert.assertTrue(Files.exists(target.resolve("file2.txt")));
  }

  @Test
  public void moveDirectoryToAnotherSystem_theChildrenInTheTargetDirectoryShouldHaveTheSameData() throws IOException {
    initRepository();
    byte[] expectedData1 = "expected data 1".getBytes();
    writeFile("/source/file1.txt", expectedData1);
    byte[] expectedData2 = "expected data 2".getBytes();
    writeFile("/source/file2.txt", expectedData2);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/source");
    GitPath target = targetGfs.getPath("/target");
    provider.move(source, target);
    Assert.assertArrayEquals(expectedData1, Files.readAllBytes(target.resolve("file1.txt")));
    Assert.assertArrayEquals(expectedData2, Files.readAllBytes(target.resolve("file2.txt")));
  }

}
