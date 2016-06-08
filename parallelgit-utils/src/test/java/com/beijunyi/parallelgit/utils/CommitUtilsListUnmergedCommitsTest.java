package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.List;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.BranchUtils.createBranch;
import static com.beijunyi.parallelgit.utils.CommitUtils.listUnmergedCommits;
import static org.eclipse.jgit.lib.Constants.MASTER;
import static org.junit.Assert.*;

public class CommitUtilsListUnmergedCommitsTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void listUnmergedCommitsWhenBranchIsAheadOfMaster_shouldReturnTheNewCommitsInBranchInReverseOrder() throws IOException {
    writeSomethingToCache();
    RevCommit masterFirst = commitToMaster();
    writeSomethingToCache();
    RevCommit branchFirst = commitToBranch("test_branch", masterFirst);
    writeSomethingToCache();
    RevCommit branchSecond = commitToBranch("test_branch");
    List<RevCommit> unmerged = listUnmergedCommits("test_branch", MASTER, repo);

    assertEquals(branchSecond, unmerged.get(0));
    assertEquals(branchFirst, unmerged.get(1));
  }

  @Test
  public void listUnmergedCommitsWhenMasterIsSameAsBranch_shouldReturnEmptyList() throws IOException {
    writeSomethingToCache();
    RevCommit masterFirst = commitToMaster();
    createBranch("test_branch", masterFirst, repo);
    List<RevCommit> unmerged = listUnmergedCommits("test_branch", MASTER, repo);

    assertTrue(unmerged.isEmpty());
  }

  @Test
  public void listUnmergedCommitsWhenMasterIsAheadOfBranch_shouldReturnEmptyList() throws IOException {
    writeSomethingToCache();
    RevCommit masterFirst = commitToMaster();
    createBranch("test_branch", masterFirst, repo);
    writeSomethingToCache();
    commitToMaster();
    List<RevCommit> unmerged = listUnmergedCommits("test_branch", MASTER, repo);

    assertTrue(unmerged.isEmpty());
  }



}
