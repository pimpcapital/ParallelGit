package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.attribute.FileStoreAttributeView;

import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.GitFileSystemProvider.GFS;
import static org.junit.Assert.*;

public class GfsFileStoreBasicTest extends PreSetupGitFileSystemTest {

  @Test
  public void getName_shouldReturnTheFileSystemSessionId() {
    assertEquals(GFS, fileStore.name());
  }

  @Test
  public void getType_shouldReturnGitfs() throws IOException {
    assertEquals(GFS, fileStore.type());
  }

  @Test
  public void testIsReadOnly_shouldReturnFalse() throws IOException {
    assertFalse(fileStore.isReadOnly());
  }

  @Test
  public void getFileStoreAttributeView_shouldReturnNull() {
    assertNull(fileStore.getFileStoreAttributeView(FileStoreAttributeView.class));
  }

}
