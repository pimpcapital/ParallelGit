package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Test;

import static org.junit.Assert.*;

public class PersistRequestTest extends PreSetupGitFileSystemTest {

  @Test
  public void persistFileChanges_fileSystemBaseTreeShouldEqualToTheResultTreeAfterTheOperation() throws IOException {
    writeSomeFileToGfs();
    AnyObjectId treeId = Gfs.persist(gfs).execute();
    assertEquals(treeId, gfs.getTree());
  }

  @Test
  public void persistFileChanges_fileSystemShouldBeNotDirtyAfterTheOperation() throws IOException {
    writeSomeFileToGfs();
    assert gfs.isDirty();
    Gfs.persist(gfs).execute();
    assertFalse(gfs.isDirty());
  }

  @Test
  public void addFileAndPersistChanges_theResultTreeShouldHaveTheAddedFile() throws IOException {
    writeToGfs("/test_file.txt");
    AnyObjectId treeId = Gfs.persist(gfs).execute();
    assert treeId != null;
    assertTrue(TreeUtils.exists("/test_file.txt", treeId, repo));
  }

}
