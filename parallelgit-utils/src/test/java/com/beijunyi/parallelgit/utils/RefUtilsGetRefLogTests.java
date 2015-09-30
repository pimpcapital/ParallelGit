package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ReflogEntry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RefUtilsGetRefLogTests extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initFileRepository(false);
  }

  private void createCommits(@Nonnull String branch, int count) throws IOException {
    for(int i = 0; i < count; i++) {
      writeSomeFileToCache();
      commitToBranch(branch, null);
    }
  }

  @Test
  public void getRefLogs_theResultShouldContainAllRefLogs() throws IOException {
    createCommits("test_branch", 3);
    Assert.assertEquals(3, RefUtils.getRefLogs("test_branch", repo).size());
  }

  @Test
  public void getRefLogsWithMaxLimit_theResultShouldContainNoMoreThanTheSpecifiedLimit() throws IOException {
    createCommits("test_branch", 3);
    Assert.assertEquals(2, RefUtils.getRefLogs("test_branch", 2, repo).size());
  }

  @Test
  public void getLastRefLog_theResultShouldReferenceToTheLastCommitCreated() throws IOException {
    writeSomeFileToCache();
    AnyObjectId firstCommit = commitToBranch("test_branch", "first commit", null);
    AnyObjectId secondLastCommit = commitToBranch("test_branch", "second commit", firstCommit);
    AnyObjectId lastCommit = commitToBranch("test_branch", "third commit", secondLastCommit);
    ReflogEntry lastRefLog = RefUtils.getLastRefLog("test_branch", repo);
    assert lastRefLog != null;
    Assert.assertEquals(lastCommit, lastRefLog.getNewId());
  }

  @Test
  public void getLastRefLogWhenThereIsNoRefLog_theResultShouldBeNull() throws IOException {
    Assert.assertNull(RefUtils.getLastRefLog("test_branch", repo));
  }

}
