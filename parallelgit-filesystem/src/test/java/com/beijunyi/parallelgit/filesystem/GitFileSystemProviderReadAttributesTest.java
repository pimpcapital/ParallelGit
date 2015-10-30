package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.io.GfsFileAttributes;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GitFileSystemProviderReadAttributesTest extends PreSetupGitFileSystemTest {

  @Test
  public void readBasicAttributes_theResultShouldContainSpecifiedAttributes() throws IOException {
    writeToGfs("/file.txt");
    Map<String, Object> attributeMap = provider.readAttributes(gfs.getPath("/file.txt"), "basic:size,isRegularFile");
    assertTrue(attributeMap.containsKey("size"));
    assertTrue(attributeMap.containsKey("isRegularFile"));
  }

  @Test
  public void readBasicAttributesWithoutViewType_theResultShouldContainSpecifiedAttributes() throws IOException {
    writeToGfs("/file.txt");
    Map<String, Object> attributeMap = provider.readAttributes(gfs.getPath("/file.txt"), "size,isRegularFile");
    assertTrue(attributeMap.containsKey("size"));
    assertTrue(attributeMap.containsKey("isRegularFile"));
  }

  @Test
  public void readPosixAttributes_theResultShouldContainSpecifiedAttributes() throws IOException {
    writeToGfs("/file.txt");
    Map<String, Object> attributeMap = provider.readAttributes(gfs.getPath("/file.txt"), "posix:permissions,owner");
    assertTrue(attributeMap.containsKey("permissions"));
    assertTrue(attributeMap.containsKey("owner"));
  }

  @Test(expected = NoSuchFileException.class)
  public void readAttributesFromNonExistentFile_shouldThrowNoSuchFileException1() throws IOException {
    provider.readAttributes(gfs.getPath("/non_existent_file.txt"), GfsFileAttributes.Basic.class);
  }


  @Test(expected = NoSuchFileException.class)
  public void readAttributesFromNonExistentFile_shouldThrowNoSuchFileException2() throws IOException {
    provider.readAttributes(gfs.getPath("/non_existent_file.txt"), "basic:size,isRegularFile");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void readUnsupportedFileAttributes_shouldThrowUnsupportedOperationException1() throws IOException {
    writeToGfs("/file.txt");
    provider.readAttributes(gfs.getPath("/file.txt"), new BasicFileAttributes() {
      @Nullable
      @Override
      public FileTime lastModifiedTime() {
        return null;
      }

      @Nullable
      @Override
      public FileTime lastAccessTime() {
        return null;
      }

      @Nullable
      @Override
      public FileTime creationTime() {
        return null;
      }

      @Override
      public boolean isRegularFile() {
        return false;
      }

      @Override
      public boolean isDirectory() {
        return false;
      }

      @Override
      public boolean isSymbolicLink() {
        return false;
      }

      @Override
      public boolean isOther() {
        return false;
      }

      @Override
      public long size() {
        return 0;
      }

      @Nullable
      @Override
      public Object fileKey() {
        return null;
      }
    }.getClass());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void readUnsupportedFileAttributes_shouldThrowUnsupportedOperationException2() throws IOException {
    writeToGfs("/file.txt");
    provider.readAttributes(gfs.getPath("/file.txt"), "some_view:some_attribute");
  }

}
