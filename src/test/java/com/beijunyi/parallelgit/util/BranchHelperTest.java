package com.beijunyi.parallelgit.util;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class BranchHelperTest extends AbstractParallelGitTest {

  @Test
  public void getBranchHeadCommitTest() throws IOException {
    ObjectId firstCommit = initRepository();
    Assert.assertEquals(firstCommit, BranchHelper.getBranchHeadCommitId(repo, Constants.MASTER));
    writeFile("a.txt");
    String branchName = "second";
    ObjectId branchCommit = commitToBranch(branchName, firstCommit);
    Assert.assertEquals(branchCommit, BranchHelper.getBranchHeadCommitId(repo, branchName));
  }

  @Test
  public void resetBranchHeadTest() throws IOException {
    ObjectId firstCommit = initRepository();
    writeFile("a.txt");
    String branchName = "second";
    ObjectId branchCommit = commitToBranch(branchName, firstCommit);
    BranchHelper.resetBranchHead(repo, Constants.MASTER, branchCommit);
    Assert.assertEquals(branchCommit, BranchHelper.getBranchHeadCommitId(repo, Constants.MASTER));
  }

}
