package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;

import com.beijunyi.parallelgit.filesystem.io.GfsSeekableByteChannel;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileSystemProviderNewByteChannelTest extends AbstractGitFileSystemTest {

  private static final byte[] ORIGINAL_TEXT_BYTES = "some plain text data".getBytes();

  @Test
  public void newReadOnlyByteChannelOfExistingFileTest() throws IOException {
    initRepository();
    writeToCache("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.READ)) {
      assertTrue(channel.isReadable());
      assertFalse(channel.isWritable());
      assertEquals(0, channel.position());
      assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
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
    writeToCache("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
    Files.newByteChannel(gfs.getPath("/dir"), StandardOpenOption.READ);
  }

  @Test
  public void newWriteOnlyByteChannelOfExistingFileTest() throws IOException {
    initRepository();
    writeToCache("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.WRITE)) {
      assertFalse(channel.isReadable());
      assertTrue(channel.isWritable());
      assertEquals(0, channel.position());
      assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
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
    writeToCache("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
    Files.newByteChannel(gfs.getPath("/dir"), StandardOpenOption.WRITE);
  }

  @Test
  public void newReadWriteByteChannelTest() throws IOException {
    initRepository();
    writeToCache("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      assertTrue(channel.isReadable());
      assertTrue(channel.isWritable());
      assertEquals(0, channel.position());
      assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
    }
  }

  @Test
  public void newByteChannelWithoutSpecifiedOpenOptionTest() throws IOException {
    initRepository();
    writeToCache("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"))) {
      assertTrue(channel.isReadable());
      assertFalse(channel.isWritable());
      assertEquals(0, channel.position());
      assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
    }
  }

  @Test
  public void newByteChannelWithAppendOpenOptionTest() throws IOException {
    initRepository();
    writeToCache("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.APPEND)) {
      assertFalse(channel.isReadable());
      assertTrue(channel.isWritable());
      assertEquals(ORIGINAL_TEXT_BYTES.length, channel.position());
      assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
    }
  }

  @Test
  public void newByteChannelOfExistingFileWithCreateOpenOptionTest() throws IOException {
    initRepository();
    writeToCache("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.CREATE)) {
      assertTrue(channel.isReadable());
      assertFalse(channel.isWritable());
      assertEquals(0, channel.position());
      assertEquals(ORIGINAL_TEXT_BYTES.length, channel.size());
    }
  }

  @Test
  public void newByteChannelOfNonExistingFileWithCreateOpenOptionTest() throws IOException {
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/file.txt"), StandardOpenOption.CREATE)) {
      assertTrue(channel.isReadable());
      assertFalse(channel.isWritable());
      assertEquals(0, channel.position());
      assertEquals(0, channel.size());
    }
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void newByteChannelOfExistingFileWithCreateNewOpenOptionTest() throws IOException {
    initRepository();
    writeToCache("dir/file.txt", ORIGINAL_TEXT_BYTES);
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
      assertTrue(channel.isReadable());
      assertFalse(channel.isWritable());
      assertEquals(0, channel.position());
      assertEquals(0, channel.size());
    }
  }

  @Test
  public void newByteChannelOfExistingFileWithTruncateExistingOpenOptionTest() throws IOException {
    initRepository();
    writeToCache("dir/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    try(GfsSeekableByteChannel channel = (GfsSeekableByteChannel) Files.newByteChannel(gfs.getPath("/dir/file.txt"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      assertTrue(channel.isWritable());
      assertFalse(channel.isReadable());
      assertEquals(0, channel.position());
      assertEquals(0, channel.size());
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void newByteChannelWithUnsupportedOpenOption() throws IOException {
    initRepository();
    writeToCache("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
    Files.newByteChannel(gfs.getPath("/dir"), StandardOpenOption.DELETE_ON_CLOSE);
  }


}
