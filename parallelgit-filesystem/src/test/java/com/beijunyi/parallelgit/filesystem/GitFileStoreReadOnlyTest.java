package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreReadOnlyTest extends AbstractGitFileSystemTest {

  @Test
  public void isReadOnly_shouldReturnFalse() throws IOException {
    initGitFileSystem();
    GitFileStore store = gfs.getFileStore();
    Assert.assertFalse(store.isReadOnly());
  }

}
