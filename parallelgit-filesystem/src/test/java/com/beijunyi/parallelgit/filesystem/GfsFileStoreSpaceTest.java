package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsFileStoreSpaceTest extends PreSetupGitFileSystemTest {

  @Test
  public void getTotalSpace_shouldReturnZero() throws IOException {
    assertEquals(0L, store.getTotalSpace());
    assertEquals(0L, store.getAttribute("totalSpace"));
  }

  @Test
  public void getUnallocatedSpace_shouldReturnZero() throws IOException {
    assertEquals(0L, store.getUnallocatedSpace());
    assertEquals(0L, store.getAttribute("unallocatedSpace"));
  }

  @Test
  public void getUsableSpace_shouldReturnTheAmountOfFreeMemory() throws IOException {
    assertEquals(Runtime.getRuntime().freeMemory(), store.getUsableSpace());
    assertEquals(Runtime.getRuntime().freeMemory(), store.getAttribute("usableSpace"));
  }


}
