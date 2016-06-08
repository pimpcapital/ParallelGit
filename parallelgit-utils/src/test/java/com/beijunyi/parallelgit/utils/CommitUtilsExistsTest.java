package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommitUtilsExistsTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void testCommitExistsWhenReferencedByCommitId_shouldReturnTrue() throws IOException {
    writeSomethingToCache();
    String id = commit().getName();
    assertTrue(CommitUtils.exists(id, repo));
  }

  @Test
  public void testHeadCommitExistsWhenReferencedByBranchName_shouldReturnTrue() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    assertTrue(CommitUtils.exists("test_branch", repo));
  }

  @Test
  public void testTagCommitExistsWhenReferencedByTagName_shouldReturnTrue() throws IOException {
    writeSomethingToCache();
    ObjectId id = commit();
    TagUtils.tagCommit("test_tag", id, repo);
    assertTrue(CommitUtils.exists("test_tag", repo));
  }

  @Test
  public void testCommitExistsWhenReferenceDoesNotExist_shouldReturnFalse() throws IOException {
    assertFalse(CommitUtils.exists("non_existent_reference", repo));
  }



}
