package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsFileStoreSpaceTest extends PreSetupGitFileSystemTest {

  @Test
  public void getTotalSpace_shouldReturnZero() throws IOException {
    assertEquals(0L, fileStore.getTotalSpace());
    assertEquals(0L, fileStore.getAttribute("totalSpace"));
  }

  @Test
  public void getUnallocatedSpace_shouldReturnZero() throws IOException {
    assertEquals(0L, fileStore.getUnallocatedSpace());
    assertEquals(0L, fileStore.getAttribute("unallocatedSpace"));
  }

  @Test
  public void getUsableSpace_shouldReturnTheAmountOfFreeMemory() throws IOException {
    assertEquals(Runtime.getRuntime().freeMemory(), fileStore.getUsableSpace());
    assertEquals(Runtime.getRuntime().freeMemory(), fileStore.getAttribute("usableSpace"));
  }


}
