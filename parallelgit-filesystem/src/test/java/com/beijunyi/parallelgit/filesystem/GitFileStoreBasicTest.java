package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreBasicTest extends PreSetupGitFileSystemTest {

  @Test
  public void getName_shouldReturnTheFileSystemSessionId() {
    Assert.assertEquals(gfs.getSessionId(), store.name());
  }

  @Test
  public void getType_shouldReturnGitfs() throws IOException {
    Assert.assertEquals("gitfs", store.type());
  }

  @Test
  public void testIsReadOnly_shouldReturnFalse() throws IOException {
    Assert.assertFalse(store.isReadOnly());
  }

}
