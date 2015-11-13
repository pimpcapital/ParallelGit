package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersistRequestTest extends PreSetupGitFileSystemTest {

  @Test
  public void persistFileChanges() throws IOException {
    writeSomeFileToGfs();
    AnyObjectId treeId = Gfs.persist(gfs).execute();
    assertEquals(treeId, gfs.getTree());
  }

}
