package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
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
    AnyObjectId commitId = CommitUtils.createCommit(someCommitMessage(), treeId, somePersonIdent(), null, repo);
    Assert.assertEquals(treeId, CommitUtils.getCommit(commitId, repo).getTree());
  }

  @Test
  public void createCommitFromCache_theResultCommitShouldHaveTheSameFilesAsInTheCache() throws IOException {
    writeFilesToCache("file1.txt", "file2.txt");
    AnyObjectId commitId = CommitUtils.createCommit(someCommitMessage(), cache, somePersonIdent(), null, repo);
    assertCacheEquals(cache, CacheUtils.forRevision(repo, commitId));
  }

  @Test
  public void createCommit_theResultCommitShouldHaveTheInputMessage() throws IOException {
    writeSomeFileToCache();
    String expectedMessage = "test message";
    AnyObjectId commitId = CommitUtils.createCommit(expectedMessage, cache, somePersonIdent(), null, repo);
    Assert.assertEquals(expectedMessage, CommitUtils.getCommit(commitId, repo).getFullMessage());
  }

  @Test
  public void createCommit_theResultCommitShouldHaveTheInputCommitter() throws IOException {
    writeSomeFileToCache();
    PersonIdent expectedCommitter = new PersonIdent("test_user", "tester@email.com");
    AnyObjectId commitId = CommitUtils.createCommit(someCommitMessage(), cache, expectedCommitter, null, repo);
    Assert.assertEquals(expectedCommitter, CommitUtils.getCommit(commitId, repo).getCommitterIdent());
  }

  @Test
  public void createCommit_theResultCommitShouldHaveTheInputAuthor() throws IOException {
    writeSomeFileToCache();
    PersonIdent expectedAuthor = new PersonIdent("test_user", "tester@email.com");
    AnyObjectId commitId = CommitUtils.createCommit(someCommitMessage(), cache, expectedAuthor, somePersonIdent(), Collections.<AnyObjectId>emptyList(), repo);
    Assert.assertEquals(expectedAuthor, CommitUtils.getCommit(commitId, repo).getAuthorIdent());
  }

  @Test
  public void createCommit_theResultCommitShouldHaveTheInputParents() throws IOException {
    AnyObjectId[] expectedParents = new AnyObjectId[] {commitToMaster(), commitToMaster()};
    writeSomeFileToCache();
    AnyObjectId commitId = CommitUtils.createCommit(someCommitMessage(), cache, somePersonIdent(), somePersonIdent(), Arrays.asList(expectedParents), repo);
    Assert.assertArrayEquals(expectedParents, CommitUtils.getCommit(commitId, repo).getParents());
  }


}
