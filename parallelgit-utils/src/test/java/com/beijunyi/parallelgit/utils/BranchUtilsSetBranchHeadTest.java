package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.RefUpdateRejectedException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BranchUtilsSetBranchHeadTest extends AbstractParallelGitTest {

  private final String branch = "test_branch";
  private RevCommit branchHead;

  @Before
  public void setUpBranch() throws IOException {
    AnyObjectId masterHead = initMemoryRepository(false);
    writeSomeFileToCache();
    branchHead =commitToBranch(branch, masterHead);
  }

  @Test
  public void commitBranchHead_branchHeadShouldBecomeTheNewCommit() throws IOException {
    writeSomeFileToCache();
    AnyObjectId childCommit = commit(branchHead);
    BranchUtils.newCommit(branch, childCommit, repo);
    Assert.assertEquals(childCommit, BranchUtils.getHeadCommit(branch, repo));
  }

  @Test(expected = RefUpdateRejectedException.class)
  public void commitBranchHeadWhenInputCommitIsNotChildCommit_shouldThrowRefUpdateRejectedException() throws IOException {
    writeSomeFileToCache();
    AnyObjectId nonChildCommit = commit(null);
    BranchUtils.newCommit(branch, nonChildCommit, repo);
  }

  @Test
  public void amendBranchHead_branchHeadShouldBecomeTheAmendedCommit() throws IOException {
    writeSomeFileToCache();
    AnyObjectId amendedCommit = commit(branchHead.getParent(0));
    BranchUtils.amendCommit(branch, amendedCommit, repo);
    Assert.assertEquals(amendedCommit, BranchUtils.getHeadCommit(branch, repo));
  }

  @Test
  public void cherryPickBranchHead_branchHeadShouldBecomeTheCherryPickedCommit() throws IOException {
    writeSomeFileToCache();
    AnyObjectId cherryPickedCommit = commit(branchHead);
    BranchUtils.cherryPickCommit(branch, cherryPickedCommit, repo);
    Assert.assertEquals(cherryPickedCommit, BranchUtils.getHeadCommit(branch, repo));
  }

  @Test(expected = RefUpdateRejectedException.class)
  public void cherryPickBranchHeadWhenInputCommitIsNotChildCommit_branchHeadShouldBecomeTheCherryPickedCommit() throws IOException {
    writeSomeFileToCache();
    AnyObjectId cherryPickedCommit = commit(null);
    BranchUtils.cherryPickCommit(branch, cherryPickedCommit, repo);
  }

}
