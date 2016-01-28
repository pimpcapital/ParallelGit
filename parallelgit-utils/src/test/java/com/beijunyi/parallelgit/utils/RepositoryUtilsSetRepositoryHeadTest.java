package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RepositoryUtilsSetRepositoryHeadTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initMemoryRepository(false);
  }

  @Test
  public void attachHeadToBranchName_theRepositoryHeadShouldBecomeTheSpecifiedBranch() throws IOException {
    String branch = "test_branch";
    commitToBranch(branch);
    RepositoryUtils.attachRepositoryHead(repo, "refs/heads/test_branch");
    Assert.assertEquals(branch, repo.getBranch());
  }

  @Test
  public void attachHeadToNewBranch_theRepositoryHeadShouldBecomeTheSpecifiedBranch() throws IOException {
    RepositoryUtils.attachRepositoryHead(repo, "refs/heads/new_branch");
    Assert.assertEquals("new_branch", repo.getBranch());
  }

  @Test
  public void attachHeadToBranchRef_theRepositoryHeadShouldBecomeTheSpecifiedBranch() throws IOException {
    String branch = "test_branch";
    commitToBranch(branch);
    Ref branchRef = repo.getRef(branch);
    RepositoryUtils.attachRepositoryHead(repo, branchRef);
    Assert.assertEquals(branch, repo.getBranch());
  }

  @Test
  public void detachHeadToCommit_theRepositoryHeadShouldBecomeTheSpecifiedCommitId() throws IOException {
    writeSomethingToCache();
    AnyObjectId commitId = commitToMaster();
    RepositoryUtils.detachRepositoryHead(repo, commitId);
    Assert.assertEquals(commitId.getName(), repo.getBranch());
  }

  @Test
  public void setHeadToBranch_theRepositoryHeadShouldAttachToTheSpecifiedBranch() throws IOException {
    String branch = "test_branch";
    commitToBranch(branch);
    RepositoryUtils.setRepositoryHead(repo, branch);
    Assert.assertEquals(branch, repo.getBranch());
  }

  @Test
  public void setHeadToNewBranch_theRepositoryHeadShouldAttachToTheSpecifiedBranch() throws IOException {
    String branch = "new_branch";
    RepositoryUtils.setRepositoryHead(repo, branch);
    Assert.assertEquals(branch, repo.getBranch());
  }

  @Test
  public void setHeadToCommit_theRepositoryHeadShouldDetachToTheSpecifiedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId commitId = commitToMaster();
    RepositoryUtils.setRepositoryHead(repo, commitId.getName());
    Assert.assertEquals(commitId.getName(), repo.getBranch());
  }

  @Test
  public void setHeadToTag_theRepositoryHeadShouldDetachToTheTaggedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId commitId = commitToMaster();
    Ref tagRef = TagUtils.tagCommit("test_tag", commitId, repo);
    RepositoryUtils.setRepositoryHead(repo, tagRef.getName());
    Assert.assertEquals(commitId.getName(), repo.getBranch());
  }


}
