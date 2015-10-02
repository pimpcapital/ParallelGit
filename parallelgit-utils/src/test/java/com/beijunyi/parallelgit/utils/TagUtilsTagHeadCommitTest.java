package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exception.RefUpdateRejectedException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TagUtilsTagHeadCommitTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initMemoryRepository(false);
  }

  @Test
  public void tagHeadCommit_theResultTagShouldPointToTheHeadCommit() throws IOException {
    writeSomeFileToCache();
    AnyObjectId expected = commit(null);
    RepositoryUtils.detachRepositoryHead(repo, expected);
    Ref tag = TagUtils.tagHeadCommit("test_tag", repo);
    Assert.assertEquals(expected, tag.getPeeledObjectId());
  }

  @Test(expected = RefUpdateRejectedException.class)
  public void tagHeadCommitWithExistingTagName_shouldThrowRefUpdateRejectedException() throws IOException {
    writeSomeFileToCache();
    TagUtils.tagCommit(commit(null), "test_tag", repo);
    writeSomeFileToCache();
    RepositoryUtils.detachRepositoryHead(repo, commit(null));
    TagUtils.tagHeadCommit("test_tag", repo);
  }

  @Test
  public void tagHeadCommitWithMessage_theResultTagShouldHaveTheInputMessage() throws IOException {
    writeSomeFileToCache();
    RepositoryUtils.detachRepositoryHead(repo, commit(null));
    Ref tag = TagUtils.tagHeadCommit("test_tag", "test_message", repo);
    Assert.assertEquals("test_message", TagUtils.getTag(tag, repo).getFullMessage());
  }


}
