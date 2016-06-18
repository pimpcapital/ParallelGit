package usecases;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.GitPath;
import org.junit.Before;
import org.junit.Test;

import static org.eclipse.jgit.lib.Constants.encodeASCII;
import static org.junit.Assert.*;

public class FilesNewByteChannelTest extends AbstractGitFileSystemTest {

  private static final byte[] ORIGINAL_TEXT_BYTES = someBytes();
  private GitPath file;

  @Before
  public void setupFileSystem() throws IOException {
    initRepository();
    writeToCache("/file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
    file = gfs.getPath("/file.txt");
  }

  @Test
  public void gitByteChannelReadTest() throws IOException {
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
      ByteBuffer buf = ByteBuffer.allocate(ORIGINAL_TEXT_BYTES.length);
      assertEquals(ORIGINAL_TEXT_BYTES.length, channel.read(buf));
      assertArrayEquals(ORIGINAL_TEXT_BYTES, buf.array());
    }
  }

  @Test
  public void gitByteChannelReadWithSmallBufferTest() throws IOException {
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
      int size = ORIGINAL_TEXT_BYTES.length / 2;
      ByteBuffer buf = ByteBuffer.allocate(size);
      assertEquals(size, channel.read(buf));
      byte[] subArrayOfOriginal = new byte[size];
      System.arraycopy(ORIGINAL_TEXT_BYTES, 0, subArrayOfOriginal, 0, size);
      assertArrayEquals(subArrayOfOriginal, buf.array());
    }
  }

  @Test
  public void gitByteChannelReadWithBigBufferTest() throws IOException {
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
      int size = ORIGINAL_TEXT_BYTES.length * 2;
      ByteBuffer buf = ByteBuffer.allocate(size);
      assertEquals(ORIGINAL_TEXT_BYTES.length, channel.read(buf));
      byte[] subArrayOfResult = new byte[ORIGINAL_TEXT_BYTES.length];
      System.arraycopy(buf.array(), 0, subArrayOfResult, 0, ORIGINAL_TEXT_BYTES.length);
      assertArrayEquals(ORIGINAL_TEXT_BYTES, subArrayOfResult);
    }
  }

  @Test
  public void gitByteChannelPartialOverwriteFromMiddleTest() throws IOException {
    int overwritePos = 5;
    byte[] data = encodeASCII("other");
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      ByteBuffer buf = ByteBuffer.wrap(data);
      channel.position(overwritePos);
      assertEquals(data.length, channel.write(buf));
    }
    byte[] expect = new byte[ORIGINAL_TEXT_BYTES.length];
    System.arraycopy(ORIGINAL_TEXT_BYTES, 0, expect, 0, ORIGINAL_TEXT_BYTES.length);
    System.arraycopy(data, 0, expect, overwritePos, data.length);
    assertArrayEquals(expect, Files.readAllBytes(file));
  }

  @Test
  public void gitByteChannelPartialOverwriteFromBeginningTest() throws IOException {
    byte[] data = encodeASCII("test");
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      ByteBuffer buf = ByteBuffer.wrap(data);
      assertEquals(data.length, channel.write(buf));
    }
    byte[] expect = new byte[ORIGINAL_TEXT_BYTES.length];
    System.arraycopy(data, 0, expect, 0, data.length);
    System.arraycopy(ORIGINAL_TEXT_BYTES, data.length, expect, data.length, ORIGINAL_TEXT_BYTES.length - data.length);
    byte[] actual = Files.readAllBytes(file);
    assertArrayEquals(expect, actual);
  }

  @Test
  public void gitByteChannelCompleteOverwriteTest() throws IOException {
    byte[] data = encodeASCII("this is a big data array that will completely overwrite");
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      ByteBuffer buf = ByteBuffer.wrap(data);
      assertEquals(data.length, channel.write(buf));
    }
    assertArrayEquals(data, Files.readAllBytes(file));
  }

  @Test
  public void gitByteChannelTruncateAfterCurrentPositionTest() throws IOException {
    int pos = 4;
    int truncatePos = 10;
    byte[] expect = new byte[truncatePos];
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      channel.position(pos);
      channel.truncate(truncatePos);
      assertEquals(truncatePos, channel.size());
      assertEquals(pos, channel.position());
      System.arraycopy(ORIGINAL_TEXT_BYTES, 0, expect, 0, truncatePos);
    }
    assertArrayEquals(expect, Files.readAllBytes(file));
  }

  @Test
  public void gitByteChannelTruncateBeforeCurrentPositionTest() throws IOException {
    int pos = 10;
    int truncatePos = 4;
    byte[] expect = new byte[truncatePos];
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      channel.position(pos);
      channel.truncate(truncatePos);
      assertEquals(truncatePos, channel.size());
      assertEquals(truncatePos, channel.position());
      System.arraycopy(ORIGINAL_TEXT_BYTES, 0, expect, 0, truncatePos);
    }
    assertArrayEquals(expect, Files.readAllBytes(file));
  }

  @Test(expected = NonReadableChannelException.class)
  public void nonReadableGitByteChannelTest() throws IOException {
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      channel.read(ByteBuffer.allocate(32));
    }
  }

  @Test(expected = NonWritableChannelException.class)
  public void nonWritableGitByteChannelTest() throws IOException {
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
      channel.write(ByteBuffer.wrap(someBytes()));
    }
  }

  @Test(expected = ClosedChannelException.class)
  public void closedGitByteChannelTest() throws IOException {
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
      channel.close();
      channel.read(ByteBuffer.allocate(32));
    }
  }


}
