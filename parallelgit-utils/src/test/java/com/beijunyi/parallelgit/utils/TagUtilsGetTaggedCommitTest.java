package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchTagException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TagUtilsGetTaggedCommitTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void getTaggedCommit_shouldReturnTheIdOfTheTaggedCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId commitId = commitToMaster();
    TagUtils.tagCommit("test_tag", commitId, repo);
    assertEquals(commitId, TagUtils.getTaggedCommit("test_tag", repo));
  }

  @Test(expected = NoSuchTagException.class)
  public void getTaggedCommitWhenTagDoesNotExist_shouldThrowNoSuchTagException() throws IOException {
    TagUtils.getTaggedCommit("non_existent_tag", repo);
  }

}
