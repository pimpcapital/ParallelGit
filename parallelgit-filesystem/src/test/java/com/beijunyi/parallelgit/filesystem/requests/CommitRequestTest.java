package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CommitRequestTest extends AbstractGitFileSystemTest {

  @Before
  public void setupGitFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void defaultCommit() throws IOException {
    Files.write(gfs.getPath("some_file.txt"), "some_content".getBytes());
    RevCommit commit = CommitRequest.prepare(gfs)
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertEquals(repo.resolve(gfs.getBranch()), commit);
  }

  @Test
  public void commitNoChange() throws IOException {
    RevCommit commit = CommitRequest.prepare(gfs)
                         .execute();
    Assert.assertNull(commit);
  }

  @Test
  public void commitWithMessage() throws IOException {
    Files.write(gfs.getPath("some_file.txt"), "some_content".getBytes());
    RevCommit commit = CommitRequest.prepare(gfs)
                         .message("test_message")
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertEquals("test_message", commit.getFullMessage());
  }

  @Test
  public void commitWithAuthor() throws IOException {
    Files.write(gfs.getPath("some_file.txt"), "some_content".getBytes());
    RevCommit commit = CommitRequest.prepare(gfs)
                         .author("test_author_name", "test_author@email.com")
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertEquals("test_author_name", commit.getAuthorIdent().getName());
    Assert.assertEquals("test_author@email.com", commit.getAuthorIdent().getEmailAddress());
  }

  @Test
  public void commitWithAuthorPersonIdent() throws IOException {
    Files.write(gfs.getPath("some_file.txt"), "some_content".getBytes());
    PersonIdent author = new PersonIdent("test_author_name", "test_author@email.com");
    RevCommit commit = CommitRequest.prepare(gfs)
                         .author(author)
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertEquals(author, commit.getAuthorIdent());
  }

  @Test
  public void commitWithCommitter() throws IOException {
    Files.write(gfs.getPath("some_file.txt"), "some_content".getBytes());
    RevCommit commit = CommitRequest.prepare(gfs)
                         .committer("test_committer_name", "test_committer@email.com")
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertEquals("test_committer_name", commit.getCommitterIdent().getName());
    Assert.assertEquals("test_committer@email.com", commit.getCommitterIdent().getEmailAddress());
    Assert.assertEquals("test_committer_name", commit.getAuthorIdent().getName());
    Assert.assertEquals("test_committer@email.com", commit.getAuthorIdent().getEmailAddress());
  }

  @Test
  public void commitWithCommitterPersonIdent() throws IOException {
    Files.write(gfs.getPath("some_file.txt"), "some_content".getBytes());
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    RevCommit commit = CommitRequest.prepare(gfs)
                         .committer(committer)
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertEquals(committer, commit.getCommitterIdent());
    Assert.assertEquals(committer, commit.getAuthorIdent());
  }

  @Test
  public void commitWithAuthorAndCommitter() throws IOException {
    Files.write(gfs.getPath("some_file.txt"), "some_content".getBytes());
    PersonIdent author = new PersonIdent("test_author_name", "test_author@email.com");
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    RevCommit commit = CommitRequest.prepare(gfs)
                         .committer(committer)
                         .author(author)
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertEquals(committer, commit.getCommitterIdent());
    Assert.assertEquals(author, commit.getAuthorIdent());
  }

  @Test
  public void commitAmend() throws IOException {
    Files.write(gfs.getPath("some_new_file.txt"), "some_new_content".getBytes());
    assert gfs.getCommit() != null;
    RevCommit[] parents = gfs.getCommit().getParents();
    RevCommit commit = CommitRequest.prepare(gfs)
                         .amend(true)
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertArrayEquals(parents, commit.getParents());
    Assert.assertEquals(repo.resolve(gfs.getBranch()), commit);
  }

  @Test
  public void commitAmendMessage() throws IOException {
    RevCommit commit = CommitRequest.prepare(gfs)
                         .message("test_message")
                         .amend(true)
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertEquals("test_message", commit.getFullMessage());
  }

  @Test
  public void commitAmendAuthor() throws IOException {
    PersonIdent author = new PersonIdent("test_author_name", "test_author@email.com");
    RevCommit commit = CommitRequest.prepare(gfs)
                         .author(author)
                         .amend(true)
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertEquals(author, commit.getAuthorIdent());
  }

  @Test
  public void commitAmendCommitter() throws IOException {
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    RevCommit commit = CommitRequest.prepare(gfs)
                         .committer(committer)
                         .amend(true)
                         .execute();
    Assert.assertNotNull(commit);
    Assert.assertEquals(committer, commit.getCommitterIdent());
  }


}
