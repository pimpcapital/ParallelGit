package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RefUtilsGetRefLogTests extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initFileRepository(false);
  }

  @Test
  public void getRefLogsWithMaxLimit_theResultShouldContainNoMoreThanTheSpecifiedLimit() throws IOException {
    String ref = RefUtils.ensureBranchRefName("test_branch");
    writeSomethingToCache();
    commitToBranch(ref, null);
    writeSomethingToCache();
    commitToBranch(ref, null);
    writeSomethingToCache();
    commitToBranch(ref, null);
    Assert.assertEquals(2, RefUtils.getRefLogs(ref, 2, repo).size());
  }

}
