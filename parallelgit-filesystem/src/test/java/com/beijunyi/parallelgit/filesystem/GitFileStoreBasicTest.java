package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileStoreBasicTest extends PreSetupGitFileSystemTest {

  @Test
  public void getName_shouldReturnTheFileSystemSessionId() {
    assertEquals(gfs.getSessionId(), store.name());
  }

  @Test
  public void getType_shouldReturnGitfs() throws IOException {
    assertEquals("gitfs", store.type());
  }

  @Test
  public void testIsReadOnly_shouldReturnFalse() throws IOException {
    assertFalse(store.isReadOnly());
  }

}
