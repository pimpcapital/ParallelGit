package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TagUtilsTagExistsTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void testTagExistsWhenTagExists_shouldReturnTrue() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("test_tag", commitToMaster(), repo);
    assertTrue(TagUtils.tagExists("test_tag", repo));
  }

  @Test
  public void testTagExistsWhenTagDoesNotExist_shouldReturnFalse() throws IOException {
    assertFalse(TagUtils.tagExists("non_existent_tag", repo));
  }

  @Test
  public void testTagExistsWhenBranchWithSameNameExists_shouldReturnFalse() throws IOException {
    writeSomethingToCache();
    commitToBranch("test");
    assertFalse(TagUtils.tagExists("test", repo));
  }


}
