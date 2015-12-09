package com.beijunyi.parallelgit.filesystem;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GfsStatusProviderSetBranchTest extends PreSetupGitFileSystemTest {

  private GfsStatusProvider statusProvider;

  @Before
  public void setUp() {
    statusProvider = gfs.getStatusProvider();
  }

  @Test
  public void setBranch_theBranchOfTheFileSystemShouldBecomeTheInputBranch() {
    gfs.getStatusProvider().branch("test_branch");
    assertEquals("test_branch", statusProvider.branch());
  }

  @Test
  public void setBranchToNull_theBranchOfTheFileSystemShouldBecomeNull() {
    statusProvider.detach();
    assertNull(statusProvider.branch());
  }
}
