package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemPersistTest extends PreSetupGitFileSystemTest {

  @Test
  public void persistWhenNoChangeIsMade_theResultShouldEqualToThePreviousTree() throws IOException {
    AnyObjectId previousTree = gfs.getTree();
    Assert.assertEquals(previousTree, gfs.persist());
  }

  @Test
  public void persistAfterChangeIsMade_theResultShouldNotEqualToThePreviousTree() throws IOException {
    AnyObjectId previousTree = gfs.getTree();
    Files.write(gfs.getPath("/some_file.txt"), "some text content".getBytes());
    Assert.assertNotEquals(previousTree, gfs.persist());
  }

  @Test
  public void persistAfterChangeIsMade_theResultShouldReflectTheChanges() throws IOException {
    byte[] expectedContent = "some text content".getBytes();
    Files.write(gfs.getPath("/some_file.txt"), expectedContent);
    AnyObjectId result = gfs.persist();
    try(TreeWalk tw = TreeWalk.forPath(repo, "some_file.txt", result)) {
      Assert.assertArrayEquals(expectedContent, repo.open(tw.getObjectId(0)).getBytes());
    }
  }

}
