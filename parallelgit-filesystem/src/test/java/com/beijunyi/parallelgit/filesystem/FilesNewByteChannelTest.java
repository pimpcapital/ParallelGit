package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import javax.annotation.Nonnull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.lang.System.arraycopy;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.*;
import static org.eclipse.jgit.lib.Constants.encodeASCII;
import static org.junit.Assert.*;

public class FilesNewByteChannelTest extends AbstractGitFileSystemTest {

  private static final byte[] TEST_DATA = someBytes();

  private SeekableByteChannel channel;

  @Before
  public void setUp() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt", TEST_DATA);
    commitToMaster();
    initGitFileSystem();
  }

  @After
  public void tearDown() throws IOException {
    if(channel != null) {
      channel.close();
      channel = null;
    }
  }

  @Test
  public void createNewReadOnlyChannelFromExistingFile_resultChannelShouldAllowReadAccess() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), READ);
    assertArrayEquals(TEST_DATA, readChannel(channel));
  }

  @Test(expected = NoSuchFileException.class)
  public void createNewReadOnlyChannelFromNonExistentFile_shouldThrowNoSuchFileException() throws IOException {
    newByteChannel(gfs.getPath("/non_existent_file.txt"), READ);
  }

  @Test(expected = AccessDeniedException.class)
  public void createNewReadOnlyChannelFromDirectory_shouldThrowAccessDeniedException() throws IOException {
    newByteChannel(gfs.getPath("/dir"), READ);
  }

  @Test
  public void createNewWriteOnlyChannelFromExistingFile_resultChannelShouldAllowWriteAccess() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), WRITE);
    writeChannel(channel, someBytes());
  }

  @Test
  public void writeToWritableChannel_shouldOverwriteFileDataFromBeginning() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), WRITE);
    byte[] overwrite = encodeASCII("!!!");
    writeChannel(channel, overwrite);
    byte[] expected = new byte[TEST_DATA.length];
    arraycopy(TEST_DATA, 0, expected, 0, TEST_DATA.length);
    arraycopy(overwrite, 0, expected, 0, overwrite.length);
    assertArrayEquals(expected, readAllBytes(gfs.getPath("/dir/file.txt")));
  }

  @Test
  public void writeToWritableChannelWithTruncateExistingOption_shouldOverwriteEntireFileData() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), WRITE, TRUNCATE_EXISTING);
    byte[] overwrite = encodeASCII("!!!");
    writeChannel(channel, overwrite);
    channel.close();
    assertArrayEquals(overwrite, readAllBytes(gfs.getPath("/dir/file.txt")));
  }

  @Test
  public void writeToWritableChannelWithAppendOption_shouldAppendDataToTheEndOfTheFile() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), WRITE, APPEND);
    byte[] append = encodeASCII("!!!");
    writeChannel(channel, append);
    channel.close();
    byte[] expected = new byte[TEST_DATA.length + append.length];
    arraycopy(TEST_DATA, 0, expected, 0, TEST_DATA.length);
    arraycopy(append, 0, expected, TEST_DATA.length, append.length);
    assertArrayEquals(expected, readAllBytes(gfs.getPath("/dir/file.txt")));
  }

  @Test(expected = NoSuchFileException.class)
  public void createNewWriteOnlyChannelFromNonExistentFile_shouldThrowNoSuchFileException() throws IOException {
    newByteChannel(gfs.getPath("/non_existent_file.txt"), WRITE);
  }

  @Test(expected = AccessDeniedException.class)
  public void createNewWriteOnlyChannelFromDirectory_shouldThrowAccessDeniedException() throws IOException {
    newByteChannel(gfs.getPath("/dir"), WRITE);
  }

  @Test
  public void createReadWriteChannel_resultChannelShouldAllowReadWriteAccess() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), READ, WRITE);
    byte[] overwrite = encodeASCII("!!!");
    writeChannel(channel, overwrite);
    byte[] expected = new byte[TEST_DATA.length];
    arraycopy(TEST_DATA, 0, expected, 0, TEST_DATA.length);
    arraycopy(overwrite, 0, expected, 0, overwrite.length);
    channel.position(0);
    assertArrayEquals(expected, readChannel(channel));
  }

  @Test
  public void createChannelWithNoExplicitOption_resultChannelShouldAllowReadAccess() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"));
    assertArrayEquals(TEST_DATA, readChannel(channel));
  }

  @Test
  public void createChannelWithReadOption_positionShouldBeTheEndOfTheFile() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), READ);
    assertEquals(0, channel.position());
  }

  @Test
  public void createChannelWithWriteOption_positionShouldBeTheEndOfTheFile() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), WRITE);
    assertEquals(0, channel.position());
  }

  @Test
  public void createChannelWithAppendOption_resultChannelShouldAllowWriteAccess() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), APPEND);
    writeChannel(channel, someBytes());
  }

  @Test
  public void createChannelWithAppendOption_positionShouldBeTheEndOfTheFile() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), APPEND);
    assertEquals(channel.size(), channel.position());
  }

  @Test
  public void createChannelWithTruncateExistingOption_sizeShouldBecomeZero() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), WRITE, TRUNCATE_EXISTING);
    assertEquals(0, channel.size());
  }

  @Test
  public void createChannelWithCreateOptionFromExistingFile_shouldReturnReadableChannel() throws IOException {
    channel = newByteChannel(gfs.getPath("/dir/file.txt"), CREATE);
    assertArrayEquals(TEST_DATA, readChannel(channel));
  }

  @Test
  public void createChannelWithCreateOptionFromNonExistentFile_shouldReturnEmptyReadableChannel() throws IOException {
    channel = newByteChannel(gfs.getPath("/non_existent_file.txt"), CREATE);
    assertArrayEquals(new byte[0], readChannel(channel));
  }

  @Test
  public void createChannelWithCreateOptionFromNonExistentFile_fileShouldBeCreated() throws IOException {
    Path newFile = gfs.getPath("/new_file.txt");
    channel = newByteChannel(newFile, CREATE);
    exists(newFile);
  }

  @Test(expected = FileAlreadyExistsException.class)
  public void createChannelWithCreateNewOptionFromExistingFile_shouldThrowFileAlreadyExistsException() throws IOException {
    newByteChannel(gfs.getPath("/dir/file.txt"), CREATE_NEW);
  }

  @Test
  public void createChannelWithCreateNewOptionFromNonExistentFile_shouldReturnEmptyReadableChannel() throws IOException {
    channel = newByteChannel(gfs.getPath("/non_existent_file.txt"), CREATE_NEW);
    assertArrayEquals(new byte[0], readChannel(channel));
  }

  @Test
  public void createChannelWithCreateNewOptionFromNonExistentFile_fileShouldBeCreated() throws IOException {
    Path newFile = gfs.getPath("/new_file.txt");
    channel = newByteChannel(newFile, CREATE_NEW);
    exists(newFile);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void createChannelWitUnsupportedOption_shouldThrowUnsupportedOperationException() throws IOException {
    newByteChannel(gfs.getPath("/dir"), DELETE_ON_CLOSE);
  }

  @Nonnull
  private static byte[] readChannel(SeekableByteChannel channel) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
    channel.read(buffer);
    return buffer.array();
  }

  private static void writeChannel(SeekableByteChannel channel, byte[] bytes) throws IOException {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    channel.write(buffer);
  }

}
