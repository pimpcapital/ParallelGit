package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import com.beijunyi.parallelgit.filesystem.hierarchy.FileNode;
import org.junit.Assert;
import org.junit.Test;

public class GitSeekableByteChannelTest {

  @Test
  public void testsIsOpenOnNewChannel_shouldReturnTrue() throws IOException {
    byte[] data = "some data".getBytes();
    try(GitSeekableByteChannel channel = new GitSeekableByteChannel(data, Collections.singleton(StandardOpenOption.READ), FileNode.newFile(false))) {
      Assert.assertTrue(channel.isOpen());
    }
  }

  @Test
  public void positionOfNewChannel_shouldReturnZero() throws IOException {
    byte[] data = "some data".getBytes();
    try(GitSeekableByteChannel channel = new GitSeekableByteChannel(data, Collections.singleton(StandardOpenOption.READ), FileNode.newFile(false))) {
      Assert.assertEquals(0, channel.position());
    }
  }

  @Test
  public void testsIsReadableOnChannelOpenedWithReadOption_shouldReturnTrue() throws IOException {
    byte[] data = "some data".getBytes();
    try(GitSeekableByteChannel channel = new GitSeekableByteChannel(data, Collections.singleton(StandardOpenOption.READ), FileNode.newFile(false))) {
      Assert.assertTrue(channel.isReadable());
    }
  }

  @Test
  public void testsIsReadableOnChannelOpenedWithoutReadOption_shouldReturnFalse() throws IOException {
    byte[] data = "some data".getBytes();
    try(GitSeekableByteChannel channel = new GitSeekableByteChannel(data, Collections.singleton(StandardOpenOption.WRITE), FileNode.newFile(false))) {
      Assert.assertFalse(channel.isReadable());
    }
  }

  @Test
  public void testsIsWritableOnChannelOpenedWithWriteOption_shouldReturnTrue() throws IOException {
    byte[] data = "some data".getBytes();
    try(GitSeekableByteChannel channel = new GitSeekableByteChannel(data, Collections.singleton(StandardOpenOption.WRITE), FileNode.newFile(false))) {
      Assert.assertTrue(channel.isWritable());
    }
  }

  @Test
  public void testsIsWritableOnChannelOpenedWithoutReadOption_shouldReturnFalse() throws IOException {
    byte[] data = "some data".getBytes();
    try(GitSeekableByteChannel channel = new GitSeekableByteChannel(data, Collections.singleton(StandardOpenOption.READ), FileNode.newFile(false))) {
      Assert.assertFalse(channel.isWritable());
    }
  }

  @Test
  public void getBytesOfNewChannel_shouldReturnTheSameDataAsTheInput() throws IOException {
    byte[] data = "some data".getBytes();
    try(GitSeekableByteChannel channel = new GitSeekableByteChannel(data, Collections.singleton(StandardOpenOption.READ), FileNode.newFile(false))) {
      Assert.assertArrayEquals(data, channel.getBytes());
    }
  }

  @Test
  public void sizeOfNewChannel_shouldReturnTheLengthOfTheInputByteArray() throws IOException {
    byte[] data = "10 bytes..".getBytes();
    try(GitSeekableByteChannel channel = new GitSeekableByteChannel(data, Collections.singleton(StandardOpenOption.READ), FileNode.newFile(false))) {
      Assert.assertEquals(data.length, channel.size());
    }
  }

}
