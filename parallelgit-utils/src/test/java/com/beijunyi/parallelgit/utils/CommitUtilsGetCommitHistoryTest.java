package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CommitUtilsGetCommitHistoryTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void getCommitHistory_shouldReturnTheCommitsInReverseOrder() throws IOException {
    String branch = "orphan_branch";
    writeSomeFileToCache();
    RevCommit commit1 = commitToBranch(branch);
    writeSomeFileToCache();
    RevCommit commit2 = commitToBranch(branch);
    writeSomeFileToCache();
    RevCommit commit3 = commitToBranch(branch);

    List<RevCommit> expected = Arrays.asList(commit3, commit2, commit1);
    List<RevCommit> actual = CommitUtils.getCommitHistory(commit3, repo);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void getCommitHistoryWithSkipAndLimit_shouldReturnTheCommitsInTheSelectedRange() throws IOException {
    String branch = "orphan_branch";
    writeSomeFileToCache();
    /*RevCommit commit1 = */commitToBranch(branch);
    writeSomeFileToCache();
    RevCommit commit2 = commitToBranch(branch);
    writeSomeFileToCache();
    RevCommit commit3 = commitToBranch(branch);
    writeSomeFileToCache();
    RevCommit commit4 = commitToBranch(branch);

    List<RevCommit> expected = Arrays.asList(commit3, commit2);
    List<RevCommit> actual = CommitUtils.getCommitHistory(commit4, 1, 2, repo.newObjectReader());
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void getCommitHistoryWhenSkipIsNotZeroAndLimitIsIntegerMax_shouldReturnTailCommits() throws IOException {
    String branch = "orphan_branch";
    writeSomeFileToCache();
    RevCommit commit1 =commitToBranch(branch);
    writeSomeFileToCache();
    RevCommit commit2 = commitToBranch(branch);
    writeSomeFileToCache();
    RevCommit commit3 = commitToBranch(branch);
    writeSomeFileToCache();
    RevCommit commit4 = commitToBranch(branch);

    List<RevCommit> expected = Arrays.asList(commit3, commit2, commit1);
    List<RevCommit> actual = CommitUtils.getCommitHistory(commit4, 1, Integer.MAX_VALUE, repo.newObjectReader());
    Assert.assertEquals(expected, actual);
  }


}
