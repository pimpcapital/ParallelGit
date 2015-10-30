package com.beijunyi.parallelgit.filesystem;

import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileSystemSetBranchTest extends PreSetupGitFileSystemTest {

  @Test
  public void setBranch_theBranchOfTheFileSystemShouldBecomeTheInputBranch() {
    gfs.setBranch("test_branch");
    assertEquals("test_branch", gfs.getBranch());
  }

  @Test
  public void setBranchToNull_theBranchOfTheFileSystemShouldBecomeNull() {
    gfs.setBranch(null);
    assertNull(gfs.getBranch());
  }
}
