package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TagUtilsTagExistsTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void testTagExistsWhenTagExists_shouldReturnTrue() throws IOException {
    writeSomeFileToCache();
    TagUtils.tagCommit("test_tag", commitToMaster(), repo);
    Assert.assertTrue(TagUtils.tagExists("test_tag", repo));
  }

  @Test
  public void testTagExistsWhenTagDoesNotExist_shouldReturnFalse() throws IOException {
    Assert.assertFalse(TagUtils.tagExists("non_existent_tag", repo));
  }

  @Test
  public void testTagExistsWhenBranchWithSameNameExists_shouldReturnFalse() throws IOException {
    writeSomeFileToCache();
    commitToBranch("test");
    Assert.assertFalse(TagUtils.tagExists("test", repo));
  }


}
