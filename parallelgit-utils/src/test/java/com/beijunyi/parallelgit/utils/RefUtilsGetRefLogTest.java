package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RefUtilsGetRefLogTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initFileRepository(false);
  }

  @Test
  public void getRefLogsWithMaxLimit_theResultShouldContainNoMoreThanTheSpecifiedLimit() throws IOException {
    String ref = RefUtils.fullBranchName("test_branch");
    writeSomethingToCache();
    commitToBranch(ref, null);
    writeSomethingToCache();
    commitToBranch(ref, null);
    writeSomethingToCache();
    commitToBranch(ref, null);
    assertEquals(2, RefUtils.getRefLogs(ref, 2, repo).size());
  }

}
