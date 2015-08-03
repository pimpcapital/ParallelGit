package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistRequestTest extends AbstractGitFileSystemTest {

  @Before
  public void setupGitFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void persistFileChanges() throws IOException {
    Files.write(gfs.getPath("some_file.txt"), "some_content".getBytes());
    AnyObjectId treeId = PersistRequest.prepare(gfs)
                           .execute();
    Assert.assertNotNull(treeId);
    Assert.assertEquals(treeId, gfs.getTree());
  }

}
