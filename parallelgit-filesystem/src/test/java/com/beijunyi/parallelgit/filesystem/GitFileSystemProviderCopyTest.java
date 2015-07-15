package com.beijunyi.parallelgit.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;

import org.eclipse.jgit.lib.Repository;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderCopyTest extends AbstractGitFileSystemTest {

  @Test
  public void copyFileCreatingNewFileTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a.txt", data);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b.txt");
    Files.copy(source, target);
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }

  @Test
  public void copyModifiedFileTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    byte[] data = "some plain text data".getBytes();
    Files.write(source, data);
    GitPath target = gfs.getPath("/b.txt");
    Files.copy(source, target);
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void copyFileToExistingFileTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a.txt", data);
    writeFile("b.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b.txt");
    Files.copy(source, target);
  }

  @Test
  public void copyFileToExistingFileWithReplaceExistingTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a.txt", data);
    writeFile("b.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b.txt");
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }

  @Test
  public void copyFileToModifiedFileWithReplaceExistingTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a.txt", data);
    writeFile("b.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b.txt");
    Files.write(target, "some content".getBytes());
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }

  @Test(expected = AccessDeniedException.class)
  public void copyFileToOpenedFileWithReplaceExistingTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    writeFile("b.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b.txt");
    try (SeekableByteChannel channel = Files.newByteChannel(target)) {
      Assert.assertNotNull(channel);
      Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void copyFileToExistingDirectoryTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a.txt", data);
    writeFile("b/c.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b");
    Files.copy(source, target);
  }

  @Test(expected = DirectoryNotEmptyException.class)
  public void copyFileToExistingDirectoryWithReplaceExistingTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a.txt", data);
    writeFile("b/c.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b");
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
  }

  @Test(expected = NoSuchFileException.class)
  public void copyNonExistentFileTest() throws IOException {
    initGitFileSystem();
    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b.txt");
    Files.copy(source, target);
  }

  @Test
  public void copyDirectoryTest() throws IOException {
    initRepository();
    writeFile("a/b.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a");
    GitPath target = gfs.getPath("/c.txt");
    Files.copy(source, target);
  }

  @Test
  public void copySameFileTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/a.txt");
    Files.copy(source, target);
  }

  @Test
  public void copyFileToForeignGitFileSystemBasedOnSameRepositoryTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a.txt", data);
    commitToMaster();
    initGitFileSystem();

    GitFileSystem targetFs = GitFileSystemBuilder.prepare()
                               .repository(repo)
                               .build();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = targetFs.getPath("/a.txt");
    Files.copy(source, target);
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }

  @Test
  public void copyModifiedFileToForeignGitFileSystemBasedOnSameRepositoryTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileSystem targetFs = GitFileSystemBuilder.prepare()
                               .repository(repo)
                               .build();

    byte[] data = "some plain text data".getBytes();
    GitPath source = gfs.getPath("/a.txt");
    Files.write(source, data);
    GitPath target = targetFs.getPath("/a.txt");
    Files.copy(source, target);
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void copyFileToExistingFileInForeignGitFileSystemBasedOnSameRepositoryTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileSystem targetFs = GitFileSystemBuilder.prepare()
                               .repository(repo)
                               .build();
    GitPath target = targetFs.getPath("/a.txt");
    Files.write(target, "some data to be replaced".getBytes());

    GitPath source = gfs.getPath("/a.txt");
    Files.copy(source, target);
  }

  @Test
  public void copyFileToExistingFileInForeignGitFileSystemBasedOnSameRepositoryWithReplaceExistingTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a.txt", data);
    commitToMaster();
    initGitFileSystem();

    GitFileSystem targetFs = GitFileSystemBuilder.prepare()
                               .repository(repo)
                               .build();
    GitPath target = targetFs.getPath("/a.txt");
    Files.write(target, "some data to be replaced".getBytes());

    GitPath source = gfs.getPath("/a.txt");
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void copyFileToExistingDirectoryInForeignGitFileSystemBasedOnSameRepositoryTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileSystem targetFs = GitFileSystemBuilder.prepare()
                               .repository(repo)
                               .build();
    Files.write(targetFs.getPath("/a/b.txt"), "some data to be replaced".getBytes());

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = targetFs.getPath("/a");
    Files.copy(source, target);
  }

  @Test(expected = DirectoryNotEmptyException.class)
  public void copyFileToExistingDirectoryInForeignGitFileSystemBasedOnSameRepositoryWithReplaceExistingTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileSystem targetFs = GitFileSystemBuilder.prepare()
                               .repository(repo)
                               .build();
    Files.write(targetFs.getPath("/a/b.txt"), "some data to be replaced".getBytes());

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = targetFs.getPath("/a");
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
  }

  @Test
  public void copyDirectoryToForeignGitFileSystemBasedOnSameRepositoryTest() throws IOException {
    initRepository();
    writeFile("a/b.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileSystem targetFs = GitFileSystemBuilder.prepare()
                               .repository(repo)
                               .build();
    GitPath source = gfs.getPath("/a");
    GitPath target = targetFs.getPath("/a");
    Files.copy(source, target);
  }

  @Test(expected = NoSuchFileException.class)
  public void copyNonExistentFileToForeignGitFileSystemBasedOnSameRepositoryTest() throws IOException {
    initGitFileSystem();

    GitFileSystem targetFs = GitFileSystemBuilder.prepare()
                               .repository(repo)
                               .build();
    GitPath source = gfs.getPath("/a");
    GitPath target = targetFs.getPath("/a");
    Files.copy(source, target);
  }

  @Test
  public void copyFileToForeignGitFileSystemBasedOnDifferentRepositoryTest() throws IOException {
    Repository repo1 = new TestRepository(getClass().getName(), new File("/repo1"), true);
    Repository repo2 = new TestRepository(getClass().getName(), new File("/repo2"), true);
    GitFileSystem sourceFs = GitFileSystemBuilder.prepare()
                               .repository(repo1)
                               .build();
    GitFileSystem targetFs = GitFileSystemBuilder.prepare()
                               .repository(repo2)
                               .build();
    GitPath source = sourceFs.getPath("/a.txt");
    GitPath target = targetFs.getPath("/a.txt");
    byte[] data = "some plain text data".getBytes();
    Files.write(source, data);
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }





}
