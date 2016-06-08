package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BranchUtilsCreateBranchRefLogTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initFileRepository(false);
  }

  @Test
  public void createBranchFromCommitName_theRefLogShouldStartWithBranchCreatedFromCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit.getName(), repo);
    ReflogEntry lastRefLog = BranchUtils.getLastLog("test_branch", repo);
    assert lastRefLog != null;
    assertTrue(lastRefLog.getComment().startsWith("branch: Created from commit"));
  }

  @Test
  public void createBranchFromCommitId_theRefLogShouldStartWithBranchCreatedFromCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit, repo);
    ReflogEntry lastRefLog = BranchUtils.getLastLog("test_branch", repo);
    assert lastRefLog != null;
    assertTrue(lastRefLog.getComment().startsWith("branch: Created from commit"));
  }

  @Test
  public void createBranchFromCommit_theRefLogShouldStartWithBranchCreatedFromCommit() throws IOException {
    writeSomethingToCache();
    RevCommit commit = commitToMaster();
    BranchUtils.createBranch("test_branch", commit, repo);
    ReflogEntry lastRefLog = BranchUtils.getLastLog("test_branch", repo);
    assert lastRefLog != null;
    assertTrue(lastRefLog.getComment().startsWith("branch: Created from commit"));
  }

  @Test
  public void createBranchFromBranchRef_theRefLogShouldStartWithBranchCreatedFromBranch() throws IOException {
    writeSomethingToCache();
    commitToBranch("source_branch");
    BranchUtils.createBranch("test_branch", repo.findRef("source_branch"), repo);
    ReflogEntry lastRefLog = BranchUtils.getLastLog("test_branch", repo);
    assert lastRefLog != null;
    assertTrue(lastRefLog.getComment().startsWith("branch: Created from branch"));
  }

  @Test
  public void createBranchFromBranchName_theRefLogShouldStartWithBranchCreatedFromBranch() throws IOException {
    writeSomethingToCache();
    commitToBranch("source_branch");
    BranchUtils.createBranch("test_branch", "source_branch", repo);
    ReflogEntry lastRefLog = BranchUtils.getLastLog("test_branch", repo);
    assert lastRefLog != null;
    assertTrue(lastRefLog.getComment().startsWith("branch: Created from branch"));
  }

  @Test
  public void createBranchFromTagRef_theHeadOfTheNewBranchShouldEqualToTheTaggedTag() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("source_tag", commitToMaster(), repo);
    BranchUtils.createBranch("test_branch", repo.findRef("source_tag"), repo);
    ReflogEntry lastRefLog = BranchUtils.getLastLog("test_branch", repo);
    assert lastRefLog != null;
    assertTrue(lastRefLog.getComment().startsWith("branch: Created from tag"));
  }

  @Test
  public void createBranchFromTagName_theHeadOfTheNewBranchShouldEqualToTheTaggedTag() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("source_tag", commitToMaster(), repo);
    BranchUtils.createBranch("test_branch", "source_tag", repo);
    ReflogEntry lastRefLog = BranchUtils.getLastLog("test_branch", repo);
    assert lastRefLog != null;
    assertTrue(lastRefLog.getComment().startsWith("branch: Created from tag"));
  }

  @Test
  public void createBranchFromTag_theHeadOfTheNewBranchShouldEqualToTheTaggedTag() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("source_tag", commitToMaster(), repo);
    BranchUtils.createBranch("test_branch", repo.resolve("source_tag"), repo);
    ReflogEntry lastRefLog = BranchUtils.getLastLog("test_branch", repo);
    assert lastRefLog != null;
    assertTrue(lastRefLog.getComment().startsWith("branch: Created from tag"));
  }

}
