package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import static java.nio.file.Files.write;
import static org.junit.Assert.*;

public class GitFileSystemFlushTest extends PreSetupGitFileSystemTest {

  @Test
  public void flushWhenNoChangeIsMade_theResultShouldEqualToThePreviousTree() throws IOException {
    AnyObjectId previousTree = gfs.getStatusProvider().commit().getTree();
    assertEquals(previousTree, gfs.flush());
  }

  @Test
  public void flushAfterChangeIsMade_theResultShouldNotEqualToThePreviousTree() throws IOException {
    AnyObjectId previousTree = gfs.getStatusProvider().commit().getTree();
    write(gfs.getPath("/some_file.txt"), someBytes());
    assertNotEquals(previousTree, gfs.flush());
  }

  @Test
  public void flushAfterChangeIsMade_theResultShouldReflectTheChanges() throws IOException {
    byte[] expectedContent = someBytes();
    write(gfs.getPath("/some_file.txt"), expectedContent);
    AnyObjectId result = gfs.flush();
    try(TreeWalk tw = TreeUtils.forPath("/some_file.txt", result, repo)) {
      assert tw != null;
      assertArrayEquals(expectedContent, repo.open(tw.getObjectId(0)).getBytes());
    }
  }

}
