package com.beijunyi.parallelgit.filesystem;

import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GfsStatusProviderBranchTest extends PreSetupGitFileSystemTest {

  private GfsStatusProvider statusProvider;

  @Before
  public void setUp() {
    statusProvider = gfs.getStatusProvider();
  }

  @Test
  public void setBranch_theFileSystemShouldBecomeAttached() {
    statusProvider.detach();
    statusProvider.branch("test_branch");
    assertTrue(statusProvider.isAttached());
  }

  @Test
  public void setBranch_theBranchOfTheFileSystemShouldBecomeTheSpecifiedBranch() {
    statusProvider.branch("test_branch");
    assertEquals("test_branch", statusProvider.branch());
  }

  @Test
  public void setBranchUsingFullBranchRef_theBranchOfTheFileSystemShouldBecomeTheSpecifiedBranch() {
    statusProvider.branch("refs/heads/test_branch");
    assertEquals("test_branch", statusProvider.branch());
  }

  @Test
  public void detach_theFileSystemShouldBecomeDetached() {
    statusProvider.detach();
    assertFalse(statusProvider.isAttached());
  }

  @Test(expected = NoBranchException.class)
  public void detachAndGetBranch_shouldThrowNoBranchException() {
    statusProvider.detach();
    statusProvider.branch();
  }
}
