package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CommitUtilsGetCommitTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void getCommitFromCommitId_theResultShouldEqualToTheCommitId() throws IOException {
    writeSomethingToCache();
    AnyObjectId commitId = commitToMaster();
    Assert.assertEquals(commitId, CommitUtils.getCommit(commitId, repo));
  }

  @Test
  public void getCommitFromTagId_theResultShouldEqualToTheTaggedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId commitId = commitToMaster();
    AnyObjectId tagId = TagUtils.tagCommit("test_tag", commitId, repo).getObjectId();
    Assert.assertEquals(commitId, CommitUtils.getCommit(tagId, repo));
  }

  @Test
  public void getCommitFromTagRef_theResultShouldEqualToTheTaggedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId commitId = commitToMaster();
    Ref tagRef = TagUtils.tagCommit("test_tag", commitId, repo);
    Assert.assertEquals(commitId, CommitUtils.getCommit(tagRef, repo));
  }

  @Test
  public void getCommitFromBranchRef_theResultShouldEqualToTheBranchHead() throws IOException {
    writeSomethingToCache();
    AnyObjectId headCommitId = commitToBranch("test_branch");
    Assert.assertEquals(headCommitId, CommitUtils.getCommit(repo.getRef("test_branch"), repo));
  }


}
