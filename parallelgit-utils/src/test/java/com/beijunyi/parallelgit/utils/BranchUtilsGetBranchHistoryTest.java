package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.List;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exception.NoSuchRefException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BranchUtilsGetBranchHistoryTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void getBranchHistory_shouldReturnAllCommitsStartedFromTheHeadCommit() throws IOException {
    String branch = "test_branch";
    AnyObjectId[] expected = new AnyObjectId[3];
    writeSomeFileToCache();
    expected[2] = commitToBranch(branch);
    writeSomeFileToCache();
    expected[1] = commitToBranch(branch, expected[2]);
    writeSomeFileToCache();
    expected[0] = commitToBranch(branch, expected[1]);
    List<RevCommit> history = BranchUtils.getBranchHistory(branch, repo);
    AnyObjectId[] actual = new AnyObjectId[3];
    history.toArray(actual);
    Assert.assertArrayEquals(expected, actual);
  }

  @Test(expected = NoSuchRefException.class)
  public void getHistoryOfNonExistentBranch_shouldThrowNoSuchRefException() throws IOException {
    String branch = "non_existent_branch";
    BranchUtils.getBranchHistory(branch, repo);
  }

}
