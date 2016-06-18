package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import static org.junit.Assert.*;

public class GfsStatusProviderIsDirtyTest extends PreSetupGitFileSystemTest {

  @Test
  public void testIsDirtyWhenStoreIsFresh_shouldReturnFalse() throws IOException {
    assertFalse(statusProvider.isDirty());
  }

  @Test
  public void testIsDirtyWhenRootLevelFileIsChanged_shouldReturnTrue() throws IOException {
    Files.write(gfs.getPath("/some_file.txt"), someBytes());
    assertTrue(statusProvider.isDirty());
  }

  @Test
  public void testIsDirtyWhenNonRootLevelFileIsChanged_shouldReturnTrue() throws IOException {
    Files.createDirectories(gfs.getPath("/dir"));
    Files.write(gfs.getPath("/dir/some_file.txt"), someBytes());
    assertTrue(statusProvider.isDirty());
  }

  @Test
  public void testIsDirtyAfterChangesAreCommitted_shouldReturnFalse() throws IOException {
    Files.write(gfs.getPath("/some_file.txt"), someBytes());
    Gfs.commit(gfs).execute();
    assertFalse(statusProvider.isDirty());
  }



}
