package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ReflogEntry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BranchUtilsGetLogTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initFileRepository(false);
  }

  private void createCommits(String branch, int count) throws IOException {
    for(int i = 0; i < count; i++) {
      writeSomethingToCache();
      commitToBranch(branch, null);
    }
  }

  @Test
  public void getLogs_theResultShouldContainAllRefLogs() throws IOException {
    createCommits("test_branch", 3);
    assertEquals(3, BranchUtils.getLogs("test_branch", repo).size());
  }

  @Test
  public void getLogsWithMaxLimit_theResultShouldContainNoMoreThanTheSpecifiedLimit() throws IOException {
    createCommits("test_branch", 3);
    assertEquals(2, BranchUtils.getLogs("test_branch", 2, repo).size());
  }

  @Test
  public void getLastLog_theResultShouldReferenceToTheLastCommitCreated() throws IOException {
    writeSomethingToCache();
    AnyObjectId firstCommit = commitToBranch("test_branch", "first commit", null);
    AnyObjectId secondLastCommit = commitToBranch("test_branch", "second commit", firstCommit);
    AnyObjectId lastCommit = commitToBranch("test_branch", "third commit", secondLastCommit);
    ReflogEntry lastRefLog = BranchUtils.getLastLog("test_branch", repo);
    assert lastRefLog != null;
    assertEquals(lastCommit, lastRefLog.getNewId());
  }

  @Test
  public void getLastLogWhenThereIsNoRefLog_theResultShouldBeNull() throws IOException {
    assertNull(BranchUtils.getLastLog("test_branch", repo));
  }

}
