package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.BranchUtils.createBranch;
import static org.eclipse.jgit.lib.Constants.MASTER;
import static org.junit.Assert.*;

public class CommitUtilsIsMergedIntoTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void testIfBranchIsMergedIntoItself_shouldReturnTrue() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");

    assertTrue(CommitUtils.isMergedInto("test_branch", "test_branch", repo));
  }

  @Test
  public void testIfBranchIsMergedIntoMasterWhenBranchIsAheadOfMaster_shouldReturnFalse() throws IOException {
    writeSomethingToCache();
    RevCommit masterFirst = commitToMaster();
    writeSomethingToCache();
    commitToBranch("test_branch", masterFirst);
    writeSomethingToCache();
    commitToBranch("test_branch");

    assertFalse(CommitUtils.isMergedInto("test_branch", MASTER, repo));
  }

  @Test
  public void testIfBranchIsMergedIntoMasterWhenMasterIsAheadOfBranch_shouldReturnTrue() throws IOException {
    writeSomethingToCache();
    RevCommit masterFirst = commitToMaster();
    createBranch("test_branch", masterFirst, repo);
    writeSomethingToCache();
    commitToMaster();

    assertTrue(CommitUtils.isMergedInto("test_branch", MASTER, repo));
  }

}
