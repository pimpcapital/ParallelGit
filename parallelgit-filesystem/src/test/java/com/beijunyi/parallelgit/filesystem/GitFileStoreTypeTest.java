package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreTypeTest extends AbstractGitFileSystemTest {

  @Test
  public void typeOfGitFileStoreCreatedWithBranchSpecifiedTest() throws IOException {
    initRepository();
    initGitFileSystemForBranch("test_branch");
    Assert.assertEquals(GitFileStore.ATTACHED, gfs.getFileStore().type());
  }

  @Test
  public void typeOfGitFileStoreCreatedWithoutBranchSpecifiedTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    ObjectId commit = commit("test commit", null);
    initGitFileSystemForRevision(commit);
    Assert.assertEquals(GitFileStore.DETACHED, gfs.getFileStore().type());
  }
}
