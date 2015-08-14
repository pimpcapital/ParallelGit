package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Assert;
import org.junit.Test;

public class PosixGfsFileAttributeViewTest extends AbstractGitFileSystemTest {

  @Test
  public void getName_shouldReturnPosix() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Posix view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Posix.class);
    Assert.assertNotNull(view);
    Assert.assertEquals("posix", view.name());
  }

}
