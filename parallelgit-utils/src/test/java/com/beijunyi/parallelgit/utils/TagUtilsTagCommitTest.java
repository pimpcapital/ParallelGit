package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.TagAlreadyExistsException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TagUtilsTagCommitTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initMemoryRepository(false);
  }

  @Test
  public void tagHeadCommit_theResultTagShouldPointToTheHeadCommit() throws IOException {
    writeSomethingToCache();
    AnyObjectId expected = commit();
    RepositoryUtils.detachRepositoryHead(repo, expected);
    Ref tag = TagUtils.tagHeadCommit("test_tag", repo);
    assertEquals(expected, tag.getPeeledObjectId());
  }

  @Test(expected = TagAlreadyExistsException.class)
  public void tagHeadCommitWithExistingTagName_shouldThrowRefUpdateRejectedException() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("test_tag", commit(), repo);
    writeSomethingToCache();
    RepositoryUtils.detachRepositoryHead(repo, commit());
    TagUtils.tagHeadCommit("test_tag", repo);
  }

}
