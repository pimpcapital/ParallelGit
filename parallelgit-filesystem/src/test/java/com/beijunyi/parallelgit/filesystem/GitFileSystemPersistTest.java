package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileSystemPersistTest extends PreSetupGitFileSystemTest {

  @Test
  public void persistWhenNoChangeIsMade_theResultShouldEqualToThePreviousTree() throws IOException {
    AnyObjectId previousTree = gfs.getTree();
    assertEquals(previousTree, gfs.flush());
  }

  @Test
  public void persistAfterChangeIsMade_theResultShouldNotEqualToThePreviousTree() throws IOException {
    AnyObjectId previousTree = gfs.getTree();
    Files.write(gfs.getPath("/some_file.txt"), "some text content".getBytes());
    assertNotEquals(previousTree, gfs.flush());
  }

  @Test
  public void persistAfterChangeIsMade_theResultShouldReflectTheChanges() throws IOException {
    byte[] expectedContent = "some text content".getBytes();
    Files.write(gfs.getPath("/some_file.txt"), expectedContent);
    AnyObjectId result = gfs.flush();
    try(TreeWalk tw = TreeUtils.forPath("/some_file.txt", result, repo)) {
      assert tw != null;
      assertArrayEquals(expectedContent, repo.open(tw.getObjectId(0)).getBytes());
    }
  }

  @Test
  public void persistChanges_theFileSystemShouldBecomeClean() throws IOException {
    Files.write(gfs.getPath("/some_file.txt"), "some text content".getBytes());
    gfs.flush();
    assertFalse(gfs.isDirty());
  }

}
