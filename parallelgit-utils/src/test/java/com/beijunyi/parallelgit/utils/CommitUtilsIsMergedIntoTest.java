package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommitUtilsIsMergedIntoTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void testIfCommitMergedIntoItself_shouldReturnTrue() throws IOException {
    RevCommit commit = commit();
    assertTrue(CommitUtils.isMergedInto(commit, commit, repo));
  }

  @Test
  public void testIfCommitMergedIntoItsParent_shouldReturnFalse() throws IOException {
    RevCommit parent = commit();
    RevCommit commit = commit(parent);
    assertFalse(CommitUtils.isMergedInto(commit, parent, repo));
  }

  @Test
  public void testIfCommitMergedIntoItsChild_shouldReturnTrue() throws IOException {
    RevCommit commit = commit();
    RevCommit child = commit(commit);
    assertTrue(CommitUtils.isMergedInto(commit, child, repo));
  }

}
