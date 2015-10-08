package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.BranchAlreadyExistsException;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchRevisionException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BranchUtilsCreateBranchTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initMemoryRepository(true);
  }

  @Test
  public void createBranch_theNewBranchShouldExistAfterOperation() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
    Assert.assertTrue(BranchUtils.branchExists("test_branch", repo));
  }

  @Test
  public void createBranchFromCommit_theHeadOfTheNewBranchShouldEqualToTheSpecifiedCommit() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
    Assert.assertEquals(commit, BranchUtils.getBranchHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromBranch_theHeadsOfTheTwoBranchesShouldEqual() throws IOException {
    writeSomeFileToCache();
    commitToBranch("source_branch");
    BranchUtils.createBranch("test_branch", "source_branch", repo);
    Assert.assertEquals(BranchUtils.getBranchHeadCommit("source_branch", repo), BranchUtils.getBranchHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromTag_theHeadOfTheNewBranchShouldEqualToTheTaggedCommit() throws IOException {
    writeSomeFileToCache();
    TagUtils.tagCommit(commitToMaster(), "source_tag", repo);
    BranchUtils.createBranch("test_branch", "source_tag", repo);
    Assert.assertEquals(TagUtils.getTaggedCommit("source_tag", repo), BranchUtils.getBranchHeadCommit("test_branch", repo));
  }

  @Test(expected = BranchAlreadyExistsException.class)
  public void createBranchWhenBranchAlreadyExists_shouldThrowBranchAlreadyExistsException() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
  }

  @Test(expected = NoSuchRevisionException.class)
  public void createBranchFromNonExistentStartPoint_shouldThrowNoSuchRevisionException() throws IOException {
    BranchUtils.createBranch("test_branch", "non_existent_start_point", repo);
  }

}
