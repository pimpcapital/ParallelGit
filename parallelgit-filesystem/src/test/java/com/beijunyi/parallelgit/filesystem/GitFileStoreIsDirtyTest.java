package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreIsDirtyTest extends PreSetupGitFileSystemTest {

  @Test
  public void testIsDirtyWhenStoreIsFresh_shouldReturnFalse() throws IOException {
    Assert.assertFalse(store.isDirty());
  }

  @Test
  public void testIsDirtyWhenRootLevelFileIsChanged_shouldReturnTrue() throws IOException {
    Files.write(gfs.getPath("/some_file.txt"), "some text content".getBytes());
    Assert.assertTrue(store.isDirty());
  }

  @Test
  public void testIsDirtyWhenNonRootLevelFileIsChanged_shouldReturnTrue() throws IOException {
    Files.createDirectories(gfs.getPath("/dir"));
    Files.write(gfs.getPath("/dir/some_file.txt"), "some text content".getBytes());
    Assert.assertTrue(store.isDirty());
  }

  @Test
  public void testIsDirtyAfterChangesArePersisted_shouldReturnFalse() throws IOException {
    Files.write(gfs.getPath("/some_file.txt"), "some text content".getBytes());
    gfs.persist();
    Assert.assertFalse(store.isDirty());
  }



}
