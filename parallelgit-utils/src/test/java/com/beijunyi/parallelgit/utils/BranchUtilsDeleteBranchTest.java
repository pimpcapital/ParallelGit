package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exception.NoSuchRefException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BranchUtilsDeleteBranchTest extends AbstractParallelGitTest {

  @Before
  public void setUpRepository() throws IOException {
    initMemoryRepository(false);
  }

  @Test
  public void deleteBranch_branchShouldNotExistAfterDeletion() throws IOException {
    writeSomeFileToCache();
    commitToBranch("test_branch");
    BranchUtils.deleteBranch("test_branch", repo);
    Assert.assertNull(repo.getRef("test_branch"));
  }

  @Test
  public void deleteBranchWithBranchRefName_branchShouldNotExistAfterDeletion() throws IOException {
    writeSomeFileToCache();
    commitToBranch("test_branch");
    BranchUtils.deleteBranch("refs/heads/test_branch", repo);
    Assert.assertNull(repo.getRef("test_branch"));
  }

  @Test
  public void deleteBranchWhenBranchIsTheRepositoryHead_branchShouldNotExistAfterDeletion() throws IOException {
    writeSomeFileToCache();
    commitToBranch("test_branch");
    RepositoryUtils.setRepositoryHead(repo, "test_branch");
    BranchUtils.deleteBranch("refs/heads/test_branch", repo);
    Assert.assertNull(repo.getRef("test_branch"));
  }

  @Test
  public void deleteBranchWhenBranchIsTheRepositoryHead_repositoryHeadShouldDetachToTheBranchHeadCommit() throws IOException {
    writeSomeFileToCache();
    AnyObjectId headCommit = commitToBranch("test_branch");
    RepositoryUtils.setRepositoryHead(repo, "test_branch");
    BranchUtils.deleteBranch("refs/heads/test_branch", repo);
    Assert.assertEquals(headCommit.name(), repo.getBranch());
  }

  @Test(expected = NoSuchRefException.class)
  public void deleteNonExistentBranch_shouldThrowNoSuchRefException() throws IOException {
    BranchUtils.deleteBranch("non_existent_branch", repo);
  }

  @Test
  public void deleteOrphanBranch_orphanBranchShouldNotBeAffected() throws IOException {
    RepositoryUtils.setRepositoryHead(repo, "orphan_branch");
    BranchUtils.deleteBranch("orphan_branch", repo);
    Assert.assertEquals("orphan_branch", repo.getBranch());
  }


}
