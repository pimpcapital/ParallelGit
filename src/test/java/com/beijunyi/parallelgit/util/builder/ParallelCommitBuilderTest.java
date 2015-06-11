package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.util.CommitHelper;
import com.beijunyi.parallelgit.util.DirCacheHelper;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Test;

public class ParallelCommitBuilderTest extends AbstractParallelGitTest {

  private void writeSomethingToCache() throws IOException {
    writeFile("something.txt");
  }

  @Test
  public void createCommitWithTreeTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    ObjectInserter inserter = repo.newObjectInserter();
    AnyObjectId treeId;
    try {
      treeId = cache.writeTree(inserter);
    } finally {
      inserter.release();
    }
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .withTree(treeId)
                          .build();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(treeId, commit.getTree());
  }

  @Test
  public void createCommitFromCacheTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    ObjectId blobId = writeFile("testfile.txt");
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .fromCache(cache)
                          .build();
    Assert.assertNotNull(commitId);
    DirCache cache = DirCacheHelper.forRevision(repo, commitId);
    Assert.assertEquals(1, cache.getEntryCount());
    Assert.assertEquals(blobId, cache.getEntry("testfile.txt").getObjectId());
  }

  @Test
  public void createEmptyCommitTest() throws IOException {
    ObjectId currentHeadId = initRepository();
    RevCommit currentHead = CommitHelper.getCommit(repo, currentHeadId);
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .withTree(currentHead.getTree())
                          .parents(currentHead)
                          .build();
    Assert.assertNull(commitId);
  }

  @Test
  public void createEmptyCommitWithAllowEmptyOptionTest() throws IOException {
    ObjectId currentHeadId = initRepository();
    RevCommit currentHead = CommitHelper.getCommit(repo, currentHeadId);
    AnyObjectId treeId = currentHead.getTree();
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .withTree(currentHead.getTree())
                          .allowEmptyCommit(true)
                          .parents(currentHead)
                          .build();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(treeId, commit.getTree());
  }

  @Test
  public void createCommitWithAuthorTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    PersonIdent author = new PersonIdent("testuser", "testuser@email.com");
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .fromCache(cache)
                          .author(author)
                          .build();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(author, commit.getAuthorIdent());
    Assert.assertEquals(author, commit.getCommitterIdent());
  }

  @Test
  public void createCommitWithAuthorNameAndEmailTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .fromCache(cache)
                          .author("testuser", "testuser@email.com")
                          .build();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    PersonIdent author = commit.getAuthorIdent();
    Assert.assertEquals(author, commit.getCommitterIdent());
    Assert.assertEquals("testuser", author.getName());
    Assert.assertEquals("testuser@email.com", author.getEmailAddress());
  }

  @Test
  public void createCommitWithCommitterTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    PersonIdent committer = new PersonIdent("testuser", "testuser@email.com");
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .fromCache(cache)
                          .committer(committer)
                          .build();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(committer, commit.getCommitterIdent());
  }

  @Test
  public void createCommitWithCommitterNameAndEmailTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .fromCache(cache)
                          .committer("testuser", "testuser@email.com")
                          .build();
    Assert.assertNotNull(commitId);
    PersonIdent committer = CommitHelper.getCommit(repo, commitId).getCommitterIdent();
    Assert.assertEquals("testuser", committer.getName());
    Assert.assertEquals("testuser@email.com", committer.getEmailAddress());
  }

  @Test
  public void createCommitWithAuthorAndCommitterTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    PersonIdent author = new PersonIdent("author", "author@email.com");
    PersonIdent committer = new PersonIdent("committer", "committer@email.com");
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .fromCache(cache)
                          .author(author)
                          .committer(committer)
                          .build();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(author, commit.getAuthorIdent());
    Assert.assertEquals(committer, commit.getCommitterIdent());
  }

  @Test
  public void createCommitWithDefaultAuthorTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    PersonIdent defaultAuthor = new PersonIdent(repo);
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .fromCache(cache)
                          .build();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    PersonIdent author = commit.getAuthorIdent();
    Assert.assertEquals(author, commit.getCommitterIdent());
    Assert.assertEquals(defaultAuthor.getName(), author.getName());
    Assert.assertEquals(defaultAuthor.getEmailAddress(), author.getEmailAddress());
  }

  @Test
  public void createCommitWithMessageTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    String commitMessage = "test message";
    ObjectId commitId = ParallelCommitBuilder.prepare(repo)
                          .fromCache(cache)
                          .message(commitMessage)
                          .build();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(commitMessage, commit.getFullMessage());
  }


}
