package com.beijunyi.parallelgit.gfs;

import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreTypeTest extends AbstractGitFileSystemTest {

  @Test
  public void typeOfGitFileStoreCreatedWithBranchSpecifiedTest() {
    initRepository();
    initGitFileSystemForBranch("test_branch");
    Assert.assertEquals(GitFileStore.ATTACHED, gfs.getFileStore().type());
  }

  @Test
  public void typeOfGitFileStoreCreatedWithoutBranchSpecifiedTest() {
    initRepository();
    writeFile("a.txt");
    ObjectId commit = commit("test commit", null);
    initGitFileSystemForRevision(commit);
    Assert.assertEquals(GitFileStore.DETACHED, gfs.getFileStore().type());
  }
}
