package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Before;
import org.junit.Test;

import static java.nio.file.StandardOpenOption.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.eclipse.jgit.lib.Constants.encodeASCII;
import static org.junit.Assert.*;

public class GfsSeekableByteChannelTest extends AbstractGitFileSystemTest {

  private static final byte[] FILE_DATA = encodeASCII("18 bytes test data");
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
    try(SeekableByteChannel channel = new GfsSeekableByteChannel(file, singleton(READ))) {
      assertTrue(channel.isOpen());
    }
  }

  @Test
  public void positionOfNewChannel_shouldReturnZero() throws IOException {
    try(SeekableByteChannel channel = new GfsSeekableByteChannel(file, singleton(READ))) {
      assertEquals(0, channel.position());
    }
  }

  @Test
  public void readFromChannelOpenedWithReadOption_shouldStreamTheFileData() throws IOException {
    try(SeekableByteChannel channel = new GfsSeekableByteChannel(file, singleton(READ))) {
      ByteBuffer buffer = ByteBuffer.allocate(FILE_DATA.length);
      channel.read(buffer);
      assertArrayEquals(FILE_DATA, buffer.array());
    }
  }

  @Test(expected = NonReadableChannelException.class)
  public void readFromChannelOpenedWithoutReadOption_shouldThrowNonReadableChannelException() throws IOException {
    try(SeekableByteChannel channel = new GfsSeekableByteChannel(file, singleton(WRITE))) {
      channel.read(ByteBuffer.allocate(FILE_DATA.length));
    }
  }

  @Test
  public void writeToChannelOpenedWithWriteOption_shouldWriteInputToFileFromBeginning() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, singleton(WRITE))) {
      ByteBuffer buffer = ByteBuffer.wrap(encodeASCII("18 chars"));
      channel.write(buffer);
      assertArrayEquals(encodeASCII("18 chars test data"), channel.getBytes());
    }
  }

  @Test(expected = NonWritableChannelException.class)
  public void writeToChannelOpenedWithoutReadOption_shouldThrowNonWritableChannelException() throws IOException {
    try(SeekableByteChannel channel = new GfsSeekableByteChannel(file, singleton(READ))) {
      channel.write(ByteBuffer.wrap(someBytes()));
    }
  }

  @Test
  public void writeToChannelOpenedWithWriteAndAppendOption_inputShouldBeAppendedToTheEndOfTheFile() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, asList(WRITE, APPEND))) {
      ByteBuffer buffer = ByteBuffer.wrap(encodeASCII(" (not anymore)"));
      channel.write(buffer);
      assertArrayEquals(encodeASCII("18 bytes test data (not anymore)"), channel.getBytes());
    }
  }

  @Test
  public void writeToChannelOpenedWithWriteAndTruncateExistingOption_fileDataShouldBeOverwrittenByTheInput() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, asList(WRITE, TRUNCATE_EXISTING))) {
      byte[] expected = encodeASCII("new short data");
      ByteBuffer buffer = ByteBuffer.wrap(expected);
      channel.write(buffer);
      assertArrayEquals(expected, channel.getBytes());
    }
  }

  @Test
  public void getBytesOfNewChannel_shouldReturnTheSameDataAsTheInput() throws IOException {
    try(GfsSeekableByteChannel channel = new GfsSeekableByteChannel(file, singleton(READ))) {
      assertArrayEquals(FILE_DATA, channel.getBytes());
    }
  }

  @Test
  public void getSizeOfNewChannel_shouldReturnTheLengthOfTheInputByteArray() throws IOException {
    try(SeekableByteChannel channel = new GfsSeekableByteChannel(file, singleton(READ))) {
      assertEquals(FILE_DATA.length, channel.size());
    }
  }

}
