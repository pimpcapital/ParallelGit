package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.RefUpdateRejectedException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

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
  public void commitBranchHead_branchHeadShouldBecomeTheNewCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId childCommit = commit(branchHead);
    BranchUtils.newCommit(branch, childCommit, repo);
    assertEquals(childCommit, BranchUtils.getHeadCommit(branch, repo));
  }

  @Test(expected = RefUpdateRejectedException.class)
  public void commitBranchHeadWhenInputCommitIsNotChildCommit_shouldThrowRefUpdateRejectedException() throws IOException {
    writeSomethingToCache();
    AnyObjectId nonChildCommit = commit();
    BranchUtils.newCommit(branch, nonChildCommit, repo);
  }

  @Test
  public void amendBranchHead_branchHeadShouldBecomeTheAmendedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId amendedCommit = commit(branchHead.getParent(0));
    BranchUtils.amendCommit(branch, amendedCommit, repo);
    assertEquals(amendedCommit, BranchUtils.getHeadCommit(branch, repo));
  }

  @Test
  public void cherryPickBranchHead_branchHeadShouldBecomeTheCherryPickedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId cherryPickedCommit = commit(branchHead);
    BranchUtils.cherryPickCommit(branch, cherryPickedCommit, repo);
    assertEquals(cherryPickedCommit, BranchUtils.getHeadCommit(branch, repo));
  }

  @Test(expected = RefUpdateRejectedException.class)
  public void cherryPickBranchHeadWhenInputCommitIsNotChildCommit_branchHeadShouldBecomeTheCherryPickedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId cherryPickedCommit = commit();
    BranchUtils.cherryPickCommit(branch, cherryPickedCommit, repo);
  }

}
