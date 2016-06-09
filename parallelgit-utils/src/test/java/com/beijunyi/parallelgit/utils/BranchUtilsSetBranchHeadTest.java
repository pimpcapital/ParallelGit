package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.RefUpdateRejectedException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.eclipse.jgit.api.MergeResult.MergeStatus.FAST_FORWARD;
import static org.junit.Assert.assertEquals;

public class BranchUtilsSetBranchHeadTest extends AbstractParallelGitTest {

  private final String branch = "test_branch";
  private RevCommit branchHead;

  @Before
  public void setUpBranch() throws IOException {
    AnyObjectId masterHead = initMemoryRepository(false);
    writeSomethingToCache();
    branchHead =commitToBranch(branch, masterHead);
  }

  @Test
  public void createNewCommit_branchHeadShouldBecomeTheNewCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId childCommit = commit(branchHead);
    BranchUtils.newCommit(branch, childCommit, repo);
    assertEquals(childCommit, BranchUtils.getHeadCommit(branch, repo));
  }

  @Test(expected = RefUpdateRejectedException.class)
  public void createNewCommitWhenNewCommitHasDifferentAncestor_shouldThrowRefUpdateRejectedException() throws IOException {
    writeSomethingToCache();
    AnyObjectId nonChildCommit = commit();
    BranchUtils.newCommit(branch, nonChildCommit, repo);
  }

  @Test
  public void amendBranchHead_branchHeadShouldBecomeTheSpecifiedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId amendedCommit = commit(branchHead.getParent(0));
    BranchUtils.amend(branch, amendedCommit, repo);
    assertEquals(amendedCommit, BranchUtils.getHeadCommit(branch, repo));
  }

  @Test
  public void cherryPickCommit_branchHeadShouldBecomeTheSpecifiedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId cherryPickedCommit = commit(branchHead);
    BranchUtils.cherryPick(branch, cherryPickedCommit, repo);
    assertEquals(cherryPickedCommit, BranchUtils.getHeadCommit(branch, repo));
  }

  @Test
  public void mergeBranch_branchHeadShouldBecomeTheSpecifiedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId branchHeadCommit = commitToBranch("test_branch", branchHead);
    BranchUtils.merge(branch, branchHeadCommit, RefUtils.getBranchRef("test_branch", repo), FAST_FORWARD.toString(), repo);
    assertEquals(branchHeadCommit, BranchUtils.getHeadCommit(branch, repo));
  }

}
