package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Map;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.Ref;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BranchUtilsGetBranchesTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void createBranchAndGetBranches_theResultShouldContainTheNewBranch() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");

    Map<String, Ref> branches = BranchUtils.getBranches(repo);
    assertTrue(branches.containsKey("test_branch"));
  }

}
