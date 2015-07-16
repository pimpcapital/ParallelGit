package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;

import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderMoveTest extends AbstractGitFileSystemTest {

  @Test
  public void moveFileTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a.txt", data);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b.txt");
    Files.move(source, target);
    Assert.assertFalse(Files.exists(source));
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void moveFileToExistingFileTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    writeFile("b.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b.txt");
    Files.move(source, target);
  }

  @Test
  public void moveFileToExistingFileWithReplaceExistingTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a.txt", data);
    writeFile("b.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b.txt");
    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    Assert.assertFalse(Files.exists(source));
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }

  @Test
  public void moveFileToItselfTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/a.txt");
    Files.move(source, target);
    Assert.assertTrue(Files.exists(source));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void moveFileToExistingDirectoryTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    writeFile("b/c.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b");
    Files.move(source, target);
  }

  @Test(expected = DirectoryNotEmptyException.class)
  public void moveFileToExistingDirectoryWithReplaceExistingTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    writeFile("b/c.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a.txt");
    GitPath target = gfs.getPath("/b");
    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
  }

  @Test
  public void moveDirectoryTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a/file.txt", data);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a");
    GitPath target = gfs.getPath("/b");
    Files.move(source, target);
    Assert.assertFalse(Files.exists(source));
    Assert.assertFalse(Files.exists(source.resolve("file.txt")));
    Assert.assertTrue(Files.exists(target));
    GitPath targetFile = target.resolve("file.txt");
    Assert.assertTrue(Files.exists(targetFile));
    Assert.assertArrayEquals(data, Files.readAllBytes(targetFile));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void moveDirectoryToExistingFileTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a/file.txt", data);
    writeFile("b", data);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a");
    GitPath target = gfs.getPath("/b");
    Files.move(source, target);
  }

  @Test
  public void moveDirectoryToExistingFileWithReplaceExistingTest() throws IOException {
    initRepository();
    byte[] data = "some plain text data".getBytes();
    writeFile("a/file.txt", data);
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a");
    GitPath target = gfs.getPath("/b");
    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    Assert.assertFalse(Files.exists(source));
    Assert.assertFalse(Files.exists(source.resolve("file.txt")));
    Assert.assertTrue(Files.exists(target));
    GitPath targetFile = target.resolve("file.txt");
    Assert.assertTrue(Files.exists(targetFile));
    Assert.assertArrayEquals(data, Files.readAllBytes(targetFile));
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void moveDirectoryToExistingDirectoryTest() throws IOException {
    initRepository();
    writeFile("a/file1.txt");
    writeFile("b/file2.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a");
    GitPath target = gfs.getPath("/b");
    Files.move(source, target);
  }

  @Test(expected = DirectoryNotEmptyException.class)
  public void moveDirectoryToExistingDirectoryWithReplaceExistingTest() throws IOException {
    initRepository();
    writeFile("a/file1.txt");
    writeFile("b/file2.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a");
    GitPath target = gfs.getPath("/b");
    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
  }

  @Test(expected = AccessDeniedException.class)
  public void moveDirectoryToDescendantTest() throws IOException {
    initRepository();
    writeFile("a/file1.txt");
    commitToMaster();
    initGitFileSystem();

    GitPath source = gfs.getPath("/a");
    GitPath target = gfs.getPath("/a/b");
    Files.move(source, target);
  }

  @Test(expected = NoSuchFileException.class)
  public void moveNonExistentFileTest() throws IOException {
    initGitFileSystem();
    GitPath source = gfs.getPath("/a");
    GitPath target = gfs.getPath("/b");
    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
  }

  @Test
  public void moveFileToForeignGitFileSystemTest() throws IOException {
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
    Files.move(source, target);
    Assert.assertFalse(Files.exists(source));
    Assert.assertTrue(Files.exists(target));
    Assert.assertArrayEquals(data, Files.readAllBytes(target));
  }

  @Test(expected = DirectoryNotEmptyException.class)
  public void moveDirectoryForeignGitFileSystemTest() throws IOException {
    initRepository();
    writeFile("a/b.txt");
    commitToMaster();
    initGitFileSystem();

    GitFileSystem targetFs = GitFileSystemBuilder.prepare()
                               .repository(repo)
                               .build();
    GitPath source = gfs.getPath("/a");
    GitPath target = targetFs.getPath("/");
    Files.move(source, target);
  }

}
