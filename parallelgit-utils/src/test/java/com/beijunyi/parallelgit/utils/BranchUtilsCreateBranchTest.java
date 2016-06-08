package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.BranchAlreadyExistsException;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchCommitException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BranchUtilsCreateBranchTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void createBranch_theNewBranchShouldExistAfterTheOperation() throws IOException {
    writeSomethingToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
    assertTrue(BranchUtils.branchExists("test_branch", repo));
  }

  @Test
  public void createBranchFromCommitId_theHeadOfTheNewBranchShouldEqualToTheSpecifiedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit, repo);
    assertEquals(commit, BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromCommitName_theHeadOfTheNewBranchShouldEqualToTheSpecifiedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
    assertEquals(commit, BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromCommit_theHeadOfTheNewBranchShouldEqualToTheSpecifiedCommit() throws IOException {
    writeSomethingToCache();
    RevCommit commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit, repo);
    assertEquals(commit, BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromBranchRef_theHeadsOfTheTwoBranchesShouldEqual() throws IOException {
    writeSomethingToCache();
    commitToBranch("source_branch");
    BranchUtils.createBranch("test_branch", repo.findRef("source_branch"), repo);
    assertEquals(BranchUtils.getHeadCommit("source_branch", repo), BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromBranchName_theHeadsOfTheTwoBranchesShouldEqual() throws IOException {
    writeSomethingToCache();
    commitToBranch("source_branch");
    BranchUtils.createBranch("test_branch", "source_branch", repo);
    assertEquals(BranchUtils.getHeadCommit("source_branch", repo), BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromTagRef_theHeadOfTheNewBranchShouldEqualToTheTaggedCommit() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("source_tag", commitToMaster(), repo);
    BranchUtils.createBranch("test_branch", repo.findRef("source_tag"), repo);
    assertEquals(TagUtils.getTaggedCommit("source_tag", repo), BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromTagName_theHeadOfTheNewBranchShouldEqualToTheTaggedCommit() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("source_tag", commitToMaster(), repo);
    BranchUtils.createBranch("test_branch", "source_tag", repo);
    assertEquals(TagUtils.getTaggedCommit("source_tag", repo), BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test
  public void createBranchFromTag_theHeadOfTheNewBranchShouldEqualToTheTaggedCommit() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("source_tag", commitToMaster(), repo);
    BranchUtils.createBranch("test_branch", repo.resolve("source_tag"), repo);
    assertEquals(TagUtils.getTaggedCommit("source_tag", repo), BranchUtils.getHeadCommit("test_branch", repo));
  }

  @Test(expected = BranchAlreadyExistsException.class)
  public void createBranchWhenBranchAlreadyExists_shouldThrowBranchAlreadyExistsException() throws IOException {
    writeSomethingToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
  }

  @Test(expected = NoSuchCommitException.class)
  public void createBranchFromNonExistentStartPoint_shouldThrowNoSuchRevisionException() throws IOException {
    BranchUtils.createBranch("test_branch", "non_existent_start_point", repo);
  }

}
