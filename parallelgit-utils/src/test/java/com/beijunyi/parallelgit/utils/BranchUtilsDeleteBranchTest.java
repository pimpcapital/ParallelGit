package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BranchUtilsDeleteBranchTest extends AbstractParallelGitTest {

  @Before
  public void setUpRepository() throws IOException {
    initMemoryRepository(false);
  }

  @Test
  public void deleteBranch_branchShouldNotExistAfterDeletion() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    BranchUtils.deleteBranch("test_branch", repo);
    assertNull(repo.findRef("test_branch"));
  }

  @Test
  public void deleteBranchWithBranchRefName_branchShouldNotExistAfterDeletion() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    BranchUtils.deleteBranch("refs/heads/test_branch", repo);
    assertNull(repo.findRef("test_branch"));
  }

  @Test
  public void deleteBranchWhenBranchIsTheRepositoryHead_branchShouldNotExistAfterDeletion() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    RepositoryUtils.setRepositoryHead(repo, "test_branch");
    BranchUtils.deleteBranch("refs/heads/test_branch", repo);
    assertNull(repo.findRef("test_branch"));
  }

  @Test
  public void deleteBranchWhenBranchIsTheRepositoryHead_repositoryHeadShouldDetachToTheBranchHeadCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId headCommit = commitToBranch("test_branch");
    RepositoryUtils.setRepositoryHead(repo, "test_branch");
    BranchUtils.deleteBranch("refs/heads/test_branch", repo);
    assertEquals(headCommit.name(), repo.getBranch());
  }

  @Test(expected = NoSuchBranchException.class)
  public void deleteNonExistentBranch_shouldThrowNoSuchBranchException() throws IOException {
    BranchUtils.deleteBranch("non_existent_branch", repo);
  }

  @Test
  public void deleteOrphanBranch_orphanBranchShouldNotBeAffected() throws IOException {
    RepositoryUtils.setRepositoryHead(repo, "orphan_branch");
    BranchUtils.deleteBranch("orphan_branch", repo);
    assertEquals("orphan_branch", repo.getBranch());
  }


}
