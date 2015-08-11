package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;

import com.beijunyi.parallelgit.filesystem.io.GfsSeekableByteChannel;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderNewByteChannelTest extends AbstractGitFileSystemTest {

  private static final byte[] ORIGINAL_TEXT_BYTES = "some plain text data".getBytes();

  @Test
  public void newReadOnlyByteChannelOfExistingFileTest() throws IOException {
    initRepository();
    writeFile("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.READ)) {
      Assert.assertTrue(channel.isReadable());
      Assert.assertFalse(channel.isWritable());
      Assert.assertEquals(0, channel.position());
      Assert.assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
    }
  }

  @Test(expected = NoSuchFileException.class)
  public void newReadOnlyByteChannelOfNonExistentFileTest() throws IOException {
    initGitFileSystem();
    Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.READ);
  }

  @Test(expected = AccessDeniedException.class)
  public void newReadOnlyByteChannelOfDirectoryTest() throws IOException {
    initRepository();
    writeFile("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
    Files.newByteChannel(gfs.getPath("/dir"), StandardOpenOption.READ);
  }

  @Test
  public void newWriteOnlyByteChannelOfExistingFileTest() throws IOException {
    initRepository();
    writeFile("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.WRITE)) {
      Assert.assertFalse(channel.isReadable());
      Assert.assertTrue(channel.isWritable());
      Assert.assertEquals(0, channel.position());
      Assert.assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
    }
  }

  @Test(expected = NoSuchFileException.class)
  public void newWriteOnlyByteChannelOfNonExistentFileTest() throws IOException {
    initGitFileSystem();
    Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.WRITE);
  }

  @Test(expected = AccessDeniedException.class)
  public void newWriteOnlyByteChannelOfDirectoryTest() throws IOException {
    initRepository();
    writeFile("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
    Files.newByteChannel(gfs.getPath("/dir"), StandardOpenOption.WRITE);
  }

  @Test
  public void newReadWriteByteChannelTest() throws IOException {
    initRepository();
    writeFile("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      Assert.assertTrue(channel.isReadable());
      Assert.assertTrue(channel.isWritable());
      Assert.assertEquals(0, channel.position());
      Assert.assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
    }
  }

  @Test
  public void newByteChannelWithoutSpecifiedOpenOptionTest() throws IOException {
    initRepository();
    writeFile("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"))) {
      Assert.assertTrue(channel.isReadable());
      Assert.assertFalse(channel.isWritable());
      Assert.assertEquals(0, channel.position());
      Assert.assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
    }
  }

  @Test
  public void newByteChannelWithAppendOpenOptionTest() throws IOException {
    initRepository();
    writeFile("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.APPEND)) {
      Assert.assertFalse(channel.isReadable());
      Assert.assertTrue(channel.isWritable());
      Assert.assertEquals(ORIGINAL_TEXT_BYTES.length, channel.position());
      Assert.assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
    }
  }

  @Test
  public void newByteChannelOfExistingFileWithCreateOpenOptionTest() throws IOException {
    initRepository();
    writeFile("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.CREATE)) {
      Assert.assertTrue(channel.isReadable());
      Assert.assertFalse(channel.isWritable());
      Assert.assertEquals(0, channel.position());
      Assert.assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
    }
  }

  @Test
  public void newByteChannelOfNonExistingFileWithCreateOpenOptionTest() throws IOException {
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/file.txt"), StandardOpenOption.CREATE)) {
      Assert.assertTrue(channel.isReadable());
      Assert.assertFalse(channel.isWritable());
      Assert.assertEquals(0, channel.position());
      Assert.assertEquals(0, channel.size());
    }
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void newByteChannelOfExistingFileWithCreateNewOpenOptionTest() throws IOException {
    initRepository();
    writeFile("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.CREATE_NEW);
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void newByteChannelOfModifiedFileWithCreateNewOpenOptionTest() throws IOException {
    initGitFileSystem();
    GitPath path = gfs.getPath("/file.txt");
    Files.write(path, "some data".getBytes());
    Files.newByteChannel(path, StandardOpenOption.CREATE_NEW);
  }

  @Test
  public void newByteChannelOfNonExistingFileWithCreateNewOpenOptionTest() throws IOException {
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/file.txt"), StandardOpenOption.CREATE_NEW)) {
      Assert.assertTrue(channel.isReadable());
      Assert.assertFalse(channel.isWritable());
      Assert.assertEquals(0, channel.position());
      Assert.assertEquals(0, channel.size());
    }
  }

  @Test
  public void newByteChannelOfExistingFileWithTruncateExistingOpenOptionTest() throws IOException {
    initRepository();
    writeFile("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      Assert.assertTrue(channel.isWritable());
      Assert.assertFalse(channel.isReadable());
      Assert.assertEquals(0, channel.position());
      Assert.assertEquals(0, channel.size());
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void newByteChannelWithUnsupportedOpenOption() throws IOException {
    initRepository();
    writeFile("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
    Files.newByteChannel(gfs.getPath("/dir"), StandardOpenOption.DELETE_ON_CLOSE);
  }


}
