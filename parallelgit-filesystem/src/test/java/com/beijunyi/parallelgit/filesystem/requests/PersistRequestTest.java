package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Test;

public class PersistRequestTest extends PreSetupGitFileSystemTest {

  @Test
  public void persistFileChanges() throws IOException {
    writeSomeFileToGfs();
    AnyObjectId treeId = Requests.persist(gfs)
                           .execute();
    Assert.assertEquals(treeId, gfs.getTree());
  }

}
