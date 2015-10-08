package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CommitUtilsCreateCommitTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initMemoryRepository(true);
  }

  @Test
  public void createCommitFromTree_theResultCommitRootTreeShouldBeTheSpecifiedTree() throws IOException {
    writeSomeFileToCache();
    AnyObjectId treeId = CacheUtils.writeTree(cache, repo);
    RevCommit commit = CommitUtils.createCommit(someCommitMessage(), treeId, somePersonIdent(), null, repo);
    Assert.assertEquals(treeId, commit.getTree());
  }

  @Test
  public void createCommitFromCache_theResultCommitShouldHaveTheSameFilesAsInTheCache() throws IOException {
    writeFilesToCache("file1.txt", "file2.txt");
    RevCommit commit = CommitUtils.createCommit(someCommitMessage(), cache, somePersonIdent(), null, repo);
    assertCacheEquals(cache, CacheUtils.forRevision(commit, repo));
  }

  @Test
  public void createCommit_theResultCommitShouldHaveTheInputMessage() throws IOException {
    writeSomeFileToCache();
    String expectedMessage = "test message";
    RevCommit commit = CommitUtils.createCommit(expectedMessage, cache, somePersonIdent(), null, repo);
    Assert.assertEquals(expectedMessage, commit.getFullMessage());
  }

  @Test
  public void createCommit_theResultCommitShouldHaveTheInputCommitter() throws IOException {
    writeSomeFileToCache();
    PersonIdent expectedCommitter = new PersonIdent("test_user", "tester@email.com");
    RevCommit commit = CommitUtils.createCommit(someCommitMessage(), cache, expectedCommitter, null, repo);
    Assert.assertEquals(expectedCommitter, commit.getCommitterIdent());
  }

  @Test
  public void createCommit_theResultCommitShouldHaveTheInputAuthor() throws IOException {
    writeSomeFileToCache();
    PersonIdent expectedAuthor = new PersonIdent("test_user", "tester@email.com");
    RevCommit commit = CommitUtils.createCommit(someCommitMessage(), cache, expectedAuthor, somePersonIdent(), Collections.<AnyObjectId>emptyList(), repo);
    Assert.assertEquals(expectedAuthor, commit.getAuthorIdent());
  }

  @Test
  public void createCommit_theResultCommitShouldHaveTheInputParents() throws IOException {
    AnyObjectId[] expectedParents = new AnyObjectId[] {commitToMaster(), commitToMaster()};
    writeSomeFileToCache();
    RevCommit commit = CommitUtils.createCommit(someCommitMessage(), cache, somePersonIdent(), somePersonIdent(), Arrays.asList(expectedParents), repo);
    Assert.assertArrayEquals(expectedParents, commit.getParents());
  }


}
