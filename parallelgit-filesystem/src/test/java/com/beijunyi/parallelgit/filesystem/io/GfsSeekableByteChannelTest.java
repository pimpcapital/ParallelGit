package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GfsSeekableByteChannelTest extends AbstractGitFileSystemTest {

  private static final byte[] FILE_DATA = "18 bytes test data".getBytes();
  private FileNode file;

  @Before
  public void setupChannel() throws IOException {
    initRepository();
    writeToCache("/file.txt", FILE_DATA);
    commitToMaster();
    initGitFileSystem();
    file = GfsIO.findFile(gfs.getPath("/file.txt"));
  }

  @Test
  public void testsIsOpenOnNewChannel_shouldReturnTrue() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, gfs, Collections.<OpenOption>singleton(StandardOpenOption.READ))) {
      assertTrue(channel.isOpen());
    }
  }

  @Test
  public void positionOfNewChannel_shouldReturnZero() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, gfs, Collections.<OpenOption>singleton(StandardOpenOption.READ))) {
      assertEquals(0, channel.position());
    }
  }

  @Test
  public void testsIsReadableOnChannelOpenedWithReadOption_shouldReturnTrue() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, gfs, Collections.<OpenOption>singleton(StandardOpenOption.READ))) {
      assertTrue(channel.isReadable());
    }
  }

  @Test
  public void testsIsReadableOnChannelOpenedWithoutReadOption_shouldReturnFalse() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, gfs, Collections.<OpenOption>singleton(StandardOpenOption.WRITE))) {
      assertFalse(channel.isReadable());
    }
  }

  @Test
  public void testsIsWritableOnChannelOpenedWithWriteOption_shouldReturnTrue() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, gfs, Collections.<OpenOption>singleton(StandardOpenOption.WRITE))) {
      assertTrue(channel.isWritable());
    }
  }

  @Test
  public void testsIsWritableOnChannelOpenedWithoutReadOption_shouldReturnFalse() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, gfs, Collections.<OpenOption>singleton(StandardOpenOption.READ))) {
      assertFalse(channel.isWritable());
    }
  }

  @Test
  public void getBytesOfNewChannel_shouldReturnTheSameDataAsTheInput() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, gfs, Collections.<OpenOption>singleton(StandardOpenOption.READ))) {
      assertArrayEquals(FILE_DATA, channel.getBytes());
    }
  }

  @Test
  public void sizeOfNewChannel_shouldReturnTheLengthOfTheInputByteArray() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, gfs, Collections.<OpenOption>singleton(StandardOpenOption.READ))) {
      assertEquals(FILE_DATA.length, channel.size());
    }
  }

}
