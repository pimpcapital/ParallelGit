package com.beijunyi.parallelgit.command;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.util.CommitHelper;
import com.beijunyi.parallelgit.util.DirCacheHelper;
import com.beijunyi.parallelgit.util.RevTreeHelper;
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
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .withTree(treeId)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(treeId, commit.getTree());
  }

  @Test
  public void createCommitFromCacheTest() throws IOException {
    initRepository();
    String testFile = "testfile.txt";
    ObjectId blobId = writeFile(testFile);
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .call();
    Assert.assertNotNull(commitId);
    DirCache cache = DirCacheHelper.forRevision(repo, commitId);
    Assert.assertEquals(1, cache.getEntryCount());
    Assert.assertEquals(blobId, cache.getEntry(testFile).getObjectId());
  }

  @Test
  public void createEmptyCommitTest() throws IOException {
    ObjectId currentHeadId = initRepository();
    RevCommit currentHead = CommitHelper.getCommit(repo, currentHeadId);
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .withTree(currentHead.getTree())
                          .parents(currentHead)
                          .call();
    Assert.assertNull(commitId);
  }

  @Test
  public void createEmptyCommitWithAllowEmptyOptionTest() throws IOException {
    ObjectId currentHeadId = initRepository();
    RevCommit currentHead = CommitHelper.getCommit(repo, currentHeadId);
    AnyObjectId treeId = currentHead.getTree();
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .withTree(currentHead.getTree())
                          .allowEmptyCommit(true)
                          .parents(currentHead)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(treeId, commit.getTree());
  }

  @Test
  public void createCommitWithAuthorTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    PersonIdent author = new PersonIdent("testuser", "testuser@email.com");
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .author(author)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(author, commit.getAuthorIdent());
  }

  @Test
  public void createCommitWithAuthorNameAndEmailTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    String authorName = "testuser";
    String authorEmail = "testuser@email.com";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .author(authorName, authorEmail)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    PersonIdent author = commit.getAuthorIdent();
    Assert.assertEquals(authorName, author.getName());
    Assert.assertEquals(authorEmail, author.getEmailAddress());
  }

  @Test
  public void createCommitWithCommitterTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    PersonIdent committer = new PersonIdent("testuser", "testuser@email.com");
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .committer(committer)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(committer, commit.getCommitterIdent());
    Assert.assertEquals(committer, commit.getAuthorIdent());
  }

  @Test
  public void createCommitWithCommitterNameAndEmailTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    String committerName = "testuser";
    String committerEmail = "testuser@email.com";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .committer(committerName, committerEmail)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    PersonIdent committer = commit.getCommitterIdent();
    Assert.assertEquals(commit.getAuthorIdent(), committer);
    Assert.assertEquals(committerName, committer.getName());
    Assert.assertEquals(committerEmail, committer.getEmailAddress());
  }

  @Test
  public void createCommitWithAuthorAndCommitterTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    PersonIdent author = new PersonIdent("author", "author@email.com");
    PersonIdent committer = new PersonIdent("committer", "committer@email.com");
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .author(author)
                          .committer(committer)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(author, commit.getAuthorIdent());
    Assert.assertEquals(committer, commit.getCommitterIdent());
  }

  @Test
  public void createCommitWithDefaultAuthorTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    PersonIdent committer = commit.getCommitterIdent();
    UserConfig userConfig = repo.getConfig().get(UserConfig.KEY);
    Assert.assertEquals(userConfig.getCommitterName(), committer.getName());
    Assert.assertEquals(userConfig.getCommitterEmail(), committer.getEmailAddress());
  }

  @Test
  public void createCommitWithMessageTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    String commitMessage = "test message";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .fromCache(cache)
                          .message(commitMessage)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit commit = CommitHelper.getCommit(repo, commitId);
    Assert.assertEquals(commitMessage, commit.getFullMessage());
  }

  @Test
  public void createCommitAmendBranchHeadMessageTest() throws IOException {
    initRepository();
    writeSomethingToCache();
    String branch = "test_branch";
    AnyObjectId treeId = RevTreeHelper.getRootTree(repo, commitToBranch(branch));
    String amendedMessage = "amended message";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .amend(true)
                          .message(amendedMessage)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(treeId, branchHead.getTree());
    Assert.assertEquals(amendedMessage, branchHead.getFullMessage());
  }

  @Test
  public void createCommitAmendBranchHeadContentTest() throws IOException {
    initRepository();
    writeFile("file1.txt");
    String branch = "test_branch";
    AnyObjectId treeId = RevTreeHelper.getRootTree(repo, commitToBranch(branch));
    String amendedMessage = "amended message";
    ObjectId commitId = ParallelCommitCommand.prepare(repo)
                          .branch(branch)
                          .amend(true)
                          .message(amendedMessage)
                          .call();
    Assert.assertNotNull(commitId);
    RevCommit branchHead = CommitHelper.getCommit(repo, branch);
    Assert.assertNotNull(branchHead);
    Assert.assertEquals(treeId, branchHead.getTree());
    Assert.assertEquals(amendedMessage, branchHead.getFullMessage());
  }



}
