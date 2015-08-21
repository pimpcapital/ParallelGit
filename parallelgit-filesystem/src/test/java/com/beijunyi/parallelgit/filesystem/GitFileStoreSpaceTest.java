package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreSpaceTest extends PreSetupGitFileSystemTest {

  @Test
  public void getTotalSpace_shouldReturnZero() throws IOException {
    Assert.assertEquals(0L, store.getTotalSpace());
    Assert.assertEquals(0L, store.getAttribute("totalSpace"));
  }

  @Test
  public void getUnallocatedSpace_shouldReturnZero() throws IOException {
    Assert.assertEquals(0L, store.getUnallocatedSpace());
    Assert.assertEquals(0L, store.getAttribute("unallocatedSpace"));
  }

  @Test
  public void getUsableSpace_shouldReturnTheAmountOfFreeMemory() throws IOException {
    Assert.assertEquals(Runtime.getRuntime().freeMemory(), store.getUsableSpace());
    Assert.assertEquals(Runtime.getRuntime().freeMemory(), store.getAttribute("usableSpace"));
  }


}
