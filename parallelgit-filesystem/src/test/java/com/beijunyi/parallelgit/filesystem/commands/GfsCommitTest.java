package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.commands.GfsCommit.Result;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import static org.junit.Assert.*;

public class GfsCommitTest extends PreSetupGitFileSystemTest {

  @Test
  public void commitInBranch_theResultCommitShouldEqualTheHeadOfTheAttachedBranch() throws IOException {
    writeSomethingToGfs();
    Result result = Gfs.commit(gfs).execute();
    assertEquals(repo.resolve(gfs.getStatusProvider().branch()), result.getCommit());
  }

  @Test
  public void commitNoChange_theResultShouldBeUnsuccessful() throws IOException {
    Result result = Gfs.commit(gfs).execute();
    assertFalse(result.isSuccessful());
  }

  @Test
  public void commitNoChangeWithAllowEmptyOption_theResultsShouldBeSuccessful() throws IOException {
    Result result = Gfs.commit(gfs).allowEmpty(true).execute();
    assertTrue(result.isSuccessful());
  }

  @Test
  public void commitNoChangeWithAllowEmptyOption_newCommitShouldBeCreated() throws IOException {
    RevCommit currentHead = gfs.getStatusProvider().commit();
    Result result = Gfs.commit(gfs).allowEmpty(true).execute();
    assertNotEquals(currentHead, result.getCommit());
  }

  @Test
  public void commitNoChangeWithAllowEmptyOption_theResultCommitShouldHaveTheSameTree() throws IOException {
    RevCommit currentHead = gfs.getStatusProvider().commit();
    Result result = Gfs.commit(gfs).allowEmpty(true).execute();
    assertEquals(currentHead.getTree(), result.getCommit().getTree());
  }

  @Test
  public void commitWithMessage_theResultCommitShouldHaveTheSpecifiedMessage() throws IOException {
    writeSomethingToGfs();
    Result result = Gfs.commit(gfs).message("test_message").execute();
    assertEquals("test_message", result.getCommit().getFullMessage());
  }

  @Test
  public void commitWithAuthor_theResultCommitShouldHaveTheSpecifiedAuthor() throws IOException {
    writeSomethingToGfs();
    PersonIdent author = new PersonIdent("test_author_name", "test_author@email.com");
    Result result = Gfs.commit(gfs).author(author).execute();
    assertEquals(author, result.getCommit().getAuthorIdent());
  }

  @Test
  public void commitWithCommitter_theResultCommitShouldHaveTheSpecifiedCommitter() throws IOException {
    writeSomethingToGfs();
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    Result result = Gfs.commit(gfs).committer(committer).execute();
    assertEquals(committer, result.getCommit().getCommitterIdent());
  }

  @Test
  public void commitWithCommitterOnly_theResultCommitAuthorShouldDefaultToBeTheSameAsTheSpecifiedCommitter() throws IOException {
    writeSomethingToGfs();
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    Result result = Gfs.commit(gfs).committer(committer).execute();
    assertEquals(committer, result.getCommit().getAuthorIdent());
  }

  @Test
  public void commitWithAuthorAndCommitter_theResultCommitShouldHaveTheSpecifiedAuthorAndCommitter() throws IOException {
    writeSomethingToGfs();
    PersonIdent author = new PersonIdent("test_author_name", "test_author@email.com");
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    Result result = Gfs.commit(gfs).committer(committer).author(author).execute();
    RevCommit resultCommit = result.getCommit();
    assertEquals(author, resultCommit.getAuthorIdent());
    assertEquals(committer, resultCommit.getCommitterIdent());
  }

  @Test
  public void amendCommit_theResultCommitShouldHaveTheSameParentsAsTheAmendedCommit() throws IOException {
    RevCommit[] parents = gfs.getStatusProvider().commit().getParents();
    writeSomethingToGfs();
    Result result = Gfs.commit(gfs).amend(true).execute();
    assertArrayEquals(parents, result.getCommit().getParents());
  }

  @Test
  public void amendInBranch_theResultCommitShouldEqualTheHeadOfTheAttachedBranch() throws IOException {
    writeSomethingToGfs();
    Result result = Gfs.commit(gfs).amend(true).execute();
    assertEquals(repo.resolve(gfs.getStatusProvider().branch()), result.getCommit());
  }

  @Test
  public void amendCommitMessage_theResultCommitShouldHaveTheNewMessage() throws IOException {
    Result result = Gfs.commit(gfs).message("test_message").amend(true).execute();
    assertEquals("test_message", result.getCommit().getFullMessage());
  }

  @Test
  public void amendCommitAuthor_theResultCommitShouldHaveTheInputAuthor() throws IOException {
    PersonIdent author = new PersonIdent("test_author_name", "test_author@email.com");
    Result result = Gfs.commit(gfs).author(author).amend(true).execute();
    assertEquals(author, result.getCommit().getAuthorIdent());
  }

  @Test
  public void amendCommitCommitter_theResultCommitShouldHaveTheInputCommitter() throws IOException {
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    Result result = Gfs.commit(gfs).committer(committer).amend(true).execute();
    assertEquals(committer, result.getCommit().getCommitterIdent());
  }



}
