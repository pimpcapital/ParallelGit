package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Map;

import com.sun.nio.zipfs.ZipFileAttributes;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderReadAttributesTest extends AbstractGitFileSystemTest {

  @Test
  public void readFileAttributesTest() throws IOException {
    initRepository();
    byte[] data = "text content".getBytes();
    writeFile("a", data);
    commitToMaster();
    initGitFileSystem();
    GitFileAttributes.Basic result = Files.readAttributes(gfs.getPath("a"), GitFileAttributes.Basic.class);
    Assert.assertEquals(GitFileAttributeView.Basic.EPOCH, result.lastModifiedTime());
    Assert.assertEquals(GitFileAttributeView.Basic.EPOCH, result.lastAccessTime());
    Assert.assertEquals(GitFileAttributeView.Basic.EPOCH, result.creationTime());
    Assert.assertTrue(result.isRegularFile());
    Assert.assertFalse(result.isDirectory());
    Assert.assertFalse(result.isSymbolicLink());
    Assert.assertFalse(result.isOther());
    Assert.assertEquals(data.length, result.size());
    Assert.assertNull(result.fileKey());
  }

  @Test
  public void readDirectoryAttributesTest() throws IOException {
    initRepository();

    writeFile("a/b");
    commitToMaster();
    initGitFileSystem();
    GitFileAttributes.Basic result = Files.readAttributes(gfs.getPath("/a"), GitFileAttributes.Basic.class);
    Assert.assertEquals(GitFileAttributeView.Basic.EPOCH, result.lastModifiedTime());
    Assert.assertEquals(GitFileAttributeView.Basic.EPOCH, result.lastAccessTime());
    Assert.assertEquals(GitFileAttributeView.Basic.EPOCH, result.creationTime());
    Assert.assertFalse(result.isRegularFile());
    Assert.assertTrue(result.isDirectory());
    Assert.assertFalse(result.isSymbolicLink());
    Assert.assertFalse(result.isOther());
    Assert.assertEquals(0, result.size());
    Assert.assertNull(result.fileKey());
  }

  @Test
  public void readRootAttributesTest() throws IOException {
    initGitFileSystem();
    GitFileAttributes.Basic result = Files.readAttributes(gfs.getPath("/"), GitFileAttributes.Basic.class);
    Assert.assertEquals(GitFileAttributeView.Basic.EPOCH, result.lastModifiedTime());
    Assert.assertEquals(GitFileAttributeView.Basic.EPOCH, result.lastAccessTime());
    Assert.assertEquals(GitFileAttributeView.Basic.EPOCH, result.creationTime());
    Assert.assertFalse(result.isRegularFile());
    Assert.assertTrue(result.isDirectory());
    Assert.assertFalse(result.isSymbolicLink());
    Assert.assertFalse(result.isOther());
    Assert.assertEquals(0, result.size());
    Assert.assertNull(result.fileKey());
  }

  @Test(expected = NoSuchFileException.class)
  public void readNonExistentFileAttributesTest() throws IOException {
    initGitFileSystem();
    Files.readAttributes(gfs.getPath("/a"), GitFileAttributes.Basic.class);
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
    Assert.assertEquals((long) data.length, attributeMap.get(GitFileAttributeView.Basic.SIZE_NAME));
    Assert.assertEquals(true, attributeMap.get(GitFileAttributeView.Basic.IS_REGULAR_FILE_NAME));
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
    Assert.assertEquals((long) data.length, attributeMap.get(GitFileAttributeView.Basic.SIZE_NAME));
    Assert.assertEquals(true, attributeMap.get(GitFileAttributeView.Basic.IS_REGULAR_FILE_NAME));
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

  @Test(expected = IllegalArgumentException.class)
  public void readInvalidAttributesTest() throws IOException {
    initRepository();
    byte[] data = "text content".getBytes();
    writeFile("a", data);
    commitToMaster();
    initGitFileSystem();
    String attributes = "customAttribute";
    Files.readAttributes(gfs.getPath("/a"), attributes);
  }
}
