package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BranchUtilsTest extends AbstractParallelGitTest {

  @Test
  public void getBranchHeadCommitTest() throws IOException {
    AnyObjectId firstCommit = initRepository();
    assertEquals(firstCommit, BranchUtils.getHeadCommit(Constants.MASTER, repo));
    writeToCache("a.txt");
    String branchName = "second";
    AnyObjectId branchCommit = commitToBranch(branchName, firstCommit);
    assertEquals(branchCommit, BranchUtils.getHeadCommit(branchName, repo));
  }

  @Test
  public void resetBranchHeadTest() throws IOException {
    AnyObjectId firstCommit = initRepository();
    writeToCache("a.txt");
    String branchName = "second";
    AnyObjectId branchCommit = commitToBranch(branchName, firstCommit);
    BranchUtils.resetBranchHead(Constants.MASTER, branchCommit, repo);
    assertEquals(branchCommit, BranchUtils.getHeadCommit(Constants.MASTER, repo));
  }

}
