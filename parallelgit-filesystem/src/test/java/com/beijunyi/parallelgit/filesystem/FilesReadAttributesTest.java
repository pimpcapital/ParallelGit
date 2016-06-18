package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Map;

import com.beijunyi.parallelgit.filesystem.io.GfsFileAttributes;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FilesReadAttributesTest extends PreSetupGitFileSystemTest {

  @Test
  public void readBasicAttributes_theResultShouldContainSpecifiedAttributes() throws IOException {
    writeToGfs("/file.txt");
    Map<String, Object> attributeMap = Files.readAttributes(gfs.getPath("/file.txt"), "basic:size,isRegularFile");
    assertTrue(attributeMap.containsKey("size"));
    assertTrue(attributeMap.containsKey("isRegularFile"));
  }

  @Test
  public void readBasicAttributesWithoutViewType_theResultShouldContainSpecifiedAttributes() throws IOException {
    writeToGfs("/file.txt");
    Map<String, Object> attributeMap = Files.readAttributes(gfs.getPath("/file.txt"), "size,isRegularFile");
    assertTrue(attributeMap.containsKey("size"));
    assertTrue(attributeMap.containsKey("isRegularFile"));
  }

  @Test
  public void readPosixAttributes_theResultShouldContainSpecifiedAttributes() throws IOException {
    writeToGfs("/file.txt");
    Map<String, Object> attributeMap = Files.readAttributes(gfs.getPath("/file.txt"), "posix:permissions,owner");
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
  public void readUnsupportedFileAttributes_shouldThrowUnsupportedOperationException2() throws IOException {
    writeToGfs("/file.txt");
    Files.readAttributes(gfs.getPath("/file.txt"), "some_view:some_attribute");
  }

}
