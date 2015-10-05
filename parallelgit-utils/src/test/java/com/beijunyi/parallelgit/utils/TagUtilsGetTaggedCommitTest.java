package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TagUtilsGetTaggedCommitTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initMemoryRepository(true);
  }

  @Test
  public void getTaggedCommit_shouldReturnTheIdOfTheTaggedCommit() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commitId = commitToMaster();
    TagUtils.tagCommit(commitId, "test_tag", repo);
    Assert.assertEquals(commitId, TagUtils.getTaggedCommit("test_tag", repo));
  }

  @Test
  public void getTaggedCommitWhenTagDoesNotExist_shouldThrowNoSuchTagException() throws IOException {
    TagUtils.getTaggedCommit("non_existent_tag", repo);
  }

}
