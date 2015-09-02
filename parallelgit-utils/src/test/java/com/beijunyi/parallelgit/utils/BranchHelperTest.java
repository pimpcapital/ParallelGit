package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.junit.Assert;
import org.junit.Test;

public class BranchHelperTest extends AbstractParallelGitTest {

  @Test
  public void getBranchHeadCommitTest() throws IOException {
    AnyObjectId firstCommit = initRepository();
    Assert.assertEquals(firstCommit, BranchHelper.getBranchHeadCommitId(Constants.MASTER, repo));
    writeToCache("a.txt");
    String branchName = "second";
    AnyObjectId branchCommit = commitToBranch(branchName, firstCommit);
    Assert.assertEquals(branchCommit, BranchHelper.getBranchHeadCommitId(branchName, repo));
  }

  @Test
  public void resetBranchHeadTest() throws IOException {
    AnyObjectId firstCommit = initRepository();
    writeToCache("a.txt");
    String branchName = "second";
    AnyObjectId branchCommit = commitToBranch(branchName, firstCommit);
    BranchHelper.resetBranchHead(Constants.MASTER, branchCommit, repo);
    Assert.assertEquals(branchCommit, BranchHelper.getBranchHeadCommitId(Constants.MASTER, repo));
  }

}
