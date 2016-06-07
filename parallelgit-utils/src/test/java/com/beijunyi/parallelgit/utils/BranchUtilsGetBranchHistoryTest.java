package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.List;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class BranchUtilsGetBranchHistoryTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void getBranchHistory_shouldReturnAllCommitsStartedFromTheHeadCommit() throws IOException {
    String branch = "test_branch";
    AnyObjectId[] expected = new AnyObjectId[3];
    writeSomethingToCache();
    expected[2] = commitToBranch(branch);
    writeSomethingToCache();
    expected[1] = commitToBranch(branch, expected[2]);
    writeSomethingToCache();
    expected[0] = commitToBranch(branch, expected[1]);
    List<RevCommit> history = BranchUtils.getHistory(branch, repo);
    AnyObjectId[] actual = new AnyObjectId[3];
    history.toArray(actual);
    assertArrayEquals(expected, actual);
  }

  @Test(expected = NoSuchBranchException.class)
  public void getHistoryOfNonExistentBranch_shouldThrowNoSuchBranchException() throws IOException {
    String branch = "non_existent_branch";
    BranchUtils.getHistory(branch, repo);
  }

}
