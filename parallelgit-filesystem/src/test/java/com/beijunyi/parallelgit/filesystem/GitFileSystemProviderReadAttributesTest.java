package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Map;

import com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView;
import com.beijunyi.parallelgit.filesystem.io.GfsFileAttributes;
import com.sun.nio.zipfs.ZipFileAttributes;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderReadAttributesTest extends AbstractGitFileSystemTest {

  @Test(expected = NoSuchFileException.class)
  public void readNonExistentFileAttributesTest() throws IOException {
    initGitFileSystem();
    Files.readAttributes(gfs.getPath("/a"), GfsFileAttributes.Basic.class);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void readUnsupportedFileAttributesTest() throws IOException {
    initGitFileSystem();
    Files.readAttributes(gfs.getPath("/a"), ZipFileAttributes.class);
  }

  @Test
  public void readSelectedAttributesWithViewNameSpecifiedTest() throws IOException {
    initRepository();
    byte[] data = "text content".getBytes();
    writeFile("a", data);
    commitToMaster();
    initGitFileSystem();
    String attributes = "basic:size,isRegularFile";
    Map<String, Object> attributeMap = Files.readAttributes(gfs.getPath("/a"), attributes);
    Assert.assertEquals(2, attributeMap.size());
    Assert.assertEquals((long) data.length, attributeMap.get(GfsFileAttributeView.Basic.SIZE_NAME));
    Assert.assertEquals(true, attributeMap.get(GfsFileAttributeView.Basic.IS_REGULAR_FILE_NAME));
  }

  @Test
  public void readSelectedAttributesWithoutViewNameSpecifiedTest() throws IOException {
    initRepository();
    byte[] data = "text content".getBytes();
    writeFile("a", data);
    commitToMaster();
    initGitFileSystem();
    String attributes = "size,isRegularFile";
    Map<String, Object> attributeMap = Files.readAttributes(gfs.getPath("/a"), attributes);
    Assert.assertEquals(2, attributeMap.size());
    Assert.assertEquals((long) data.length, attributeMap.get(GfsFileAttributeView.Basic.SIZE_NAME));
    Assert.assertEquals(true, attributeMap.get(GfsFileAttributeView.Basic.IS_REGULAR_FILE_NAME));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void readSelectedAttributesWithUnsupportedViewNameSpecifiedTest() throws IOException {
    initRepository();
    byte[] data = "text content".getBytes();
    writeFile("a", data);
    commitToMaster();
    initGitFileSystem();
    String attributes = "custom:size,isRegularFile";
    Files.readAttributes(gfs.getPath("/a"), attributes);
  }

}
