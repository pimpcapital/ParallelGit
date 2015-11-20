package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.BranchAlreadyExistsException;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchRevisionException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BranchUtilsCreateBranchTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void createBranch_theNewBranchShouldExistAfterTheOperation() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
    Assert.assertTrue(BranchUtils.branchExists("test_branch", repo));
  }

  @Test
  public void createBranchFromCommitId_theHeadOfTheNewBranchShouldEqualToTheSpecifiedCommit() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit, repo);
    Assert.assertEquals(commit, BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromCommitName_theHeadOfTheNewBranchShouldEqualToTheSpecifiedCommit() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
    Assert.assertEquals(commit, BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromCommit_theHeadOfTheNewBranchShouldEqualToTheSpecifiedCommit() throws IOException {
    writeSomeFileToCache();
    RevCommit commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit, repo);
    Assert.assertEquals(commit, BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromBranchRef_theHeadsOfTheTwoBranchesShouldEqual() throws IOException {
    writeSomeFileToCache();
    commitToBranch("source_branch");
    BranchUtils.createBranch("test_branch", repo.getRef("source_branch"), repo);
    Assert.assertEquals(BranchUtils.getHeadCommit("source_branch", repo), BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromBranchName_theHeadsOfTheTwoBranchesShouldEqual() throws IOException {
    writeSomeFileToCache();
    commitToBranch("source_branch");
    BranchUtils.createBranch("test_branch", "source_branch", repo);
    Assert.assertEquals(BranchUtils.getHeadCommit("source_branch", repo), BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromTagRef_theHeadOfTheNewBranchShouldEqualToTheTaggedCommit() throws IOException {
    writeSomeFileToCache();
    TagUtils.tagCommit("source_tag", commitToMaster(), repo);
    BranchUtils.createBranch("test_branch", repo.getRef("source_tag"), repo);
    Assert.assertEquals(TagUtils.getTaggedCommit("source_tag", repo), BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromTagName_theHeadOfTheNewBranchShouldEqualToTheTaggedCommit() throws IOException {
    writeSomeFileToCache();
    TagUtils.tagCommit("source_tag", commitToMaster(), repo);
    BranchUtils.createBranch("test_branch", "source_tag", repo);
    Assert.assertEquals(TagUtils.getTaggedCommit("source_tag", repo), BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromTag_theHeadOfTheNewBranchShouldEqualToTheTaggedCommit() throws IOException {
    writeSomeFileToCache();
    TagUtils.tagCommit("source_tag", commitToMaster(), repo);
    BranchUtils.createBranch("test_branch", repo.resolve("source_tag"), repo);
    Assert.assertEquals(TagUtils.getTaggedCommit("source_tag", repo), BranchUtils.getHeadCommit("test_branch", repo));
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
