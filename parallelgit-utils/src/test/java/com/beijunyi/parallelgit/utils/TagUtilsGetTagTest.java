package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevTag;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TagUtilsGetTagTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void getTagWhenTagExists_theResultShouldBeNotNull() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("test_tag", commitToMaster(), repo);
    assertNotNull(TagUtils.getTag("test_tag", repo));
  }

  @Test
  public void getTag_theResultTagObjectShouldHaveTheTagName() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("test_tag", commitToMaster(), repo);
    RevTag tag = TagUtils.getTag("test_tag", repo);
    assert tag != null;
    assertEquals("test_tag",tag.getTagName());
  }

  @Test
  public void getTag_theResultTagObjectShouldHaveTheTagMessage() throws IOException {
    writeSomethingToCache();
    TagUtils.tagCommit("test_tag", commitToMaster(), "test message", repo);
    RevTag tag = TagUtils.getTag("test_tag", repo);
    assert tag != null;
    assertEquals("test message", tag.getFullMessage());
  }

  @Test
  public void getTag_theResultTagObjectShouldHaveTheTagger() throws IOException {
    writeSomethingToCache();
    PersonIdent tagger = new PersonIdent("tagger", "tagger@email.com");
    TagUtils.tagCommit("test_tag", commitToMaster(), "test message", tagger, repo);
    RevTag tag = TagUtils.getTag("test_tag", repo);
    assert tag != null;
    assertEquals(tagger, tag.getTaggerIdent());
  }

  @Test
  public void getTagWhenTagDoesNotExist_theResultShouldBeNull() throws IOException {
    assertNull(TagUtils.getTag("non_existent_tag", repo));
  }

}
