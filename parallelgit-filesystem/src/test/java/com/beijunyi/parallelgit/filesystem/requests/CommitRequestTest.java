package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.utils.RevTreeUtils;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Test;

public class CommitRequestTest extends PreSetupGitFileSystemTest {

  @Test
  public void commitInBranch_theResultCommitShouldBecomeTheHeadOfBranch() throws IOException {
    writeSomeFileToGfs();
    RevCommit commit = Requests.commit(gfs)
                         .execute();
    assert gfs.getBranch() != null;
    Assert.assertEquals(repo.resolve(gfs.getBranch()), commit);
  }

  @Test
  public void commitNoChange_shouldReturnNull() throws IOException {
    RevCommit commit = Requests.commit(gfs)
                         .execute();
    Assert.assertNull(commit);
  }

  @Test
  public void commitNoChangeWithAllowEmptyOption_shouldReturnNonNull() throws IOException {
    RevCommit commit = Requests.commit(gfs)
                         .allowEmpty(true)
                         .execute();
    Assert.assertNotNull(commit);
  }

  @Test
  public void commitNoChangeWithAllowEmptyOption_rootTreeOfTheResultCommitShouldBeTheSameAsParent() throws IOException {
    RevCommit commit = Requests.commit(gfs)
                         .allowEmpty(true)
                         .execute();
    assert commit != null;
    Assert.assertEquals(RevTreeUtils.getRootTree(repo, commit.getParent(0)), commit.getTree());
  }

  @Test
  public void commitWithMessage_theResultCommitShouldHaveTheInputMessage() throws IOException {
    writeSomeFileToGfs();
    RevCommit commit = Requests.commit(gfs)
                         .message("test_message")
                         .execute();
    assert commit != null;
    Assert.assertEquals("test_message", commit.getFullMessage());
  }

  @Test
  public void commitWithAuthor_theResultCommitShouldHaveTheInputAuthor() throws IOException {
    writeSomeFileToGfs();
    PersonIdent author = new PersonIdent("test_author_name", "test_author@email.com");
    RevCommit commit = Requests.commit(gfs)
                         .author(author)
                         .execute();
    assert commit != null;
    Assert.assertEquals(author, commit.getAuthorIdent());
  }

  @Test
  public void commitWithCommitter_theResultCommitShouldHaveTheInputCommitter() throws IOException {
    writeSomeFileToGfs();
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    RevCommit commit = Requests.commit(gfs)
                         .committer(committer)
                         .execute();
    assert commit != null;
    Assert.assertEquals(committer, commit.getCommitterIdent());
  }

  @Test
  public void commitWithCommitterOnly_theResultCommitAuthorShouldDefaultToTheInputCommitter() throws IOException {
    writeSomeFileToGfs();
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    RevCommit commit = Requests.commit(gfs)
                         .committer(committer)
                         .execute();
    assert commit != null;
    Assert.assertEquals(committer, commit.getAuthorIdent());
  }

  @Test
  public void commitWithAuthorAndCommitter_theResultCommitShouldHaveTheInputAuthorAndCommitter() throws IOException {
    writeSomeFileToGfs();
    PersonIdent author = new PersonIdent("test_author_name", "test_author@email.com");
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    RevCommit commit = Requests.commit(gfs)
                         .committer(committer)
                         .author(author)
                         .execute();
    assert commit != null;
    Assert.assertEquals(author, commit.getAuthorIdent());
  }

  @Test
  public void amendCommit_theResultCommitShouldHaveTheSameParentCommitsAsTheAmendedCommit() throws IOException {
    writeSomeFileToGfs();
    assert gfs.getCommit() != null;
    RevCommit[] parents = gfs.getCommit().getParents();
    RevCommit commit = Requests.commit(gfs)
                         .amend(true)
                         .execute();
    assert commit != null;
    Assert.assertArrayEquals(parents, commit.getParents());
  }

  @Test
  public void amendInBranch_theResultCommitShouldBecomeTheHeadOfBranch() throws IOException {
    writeSomeFileToGfs();
    RevCommit commit = Requests.commit(gfs)
                         .amend(true)
                         .execute();
    assert gfs.getBranch() != null;
    Assert.assertEquals(repo.resolve(gfs.getBranch()), commit);
  }

  @Test
  public void amendCommitMessage_theResultCommitShouldHaveTheInputMessage() throws IOException {
    RevCommit commit = Requests.commit(gfs)
                         .message("test_message")
                         .amend(true)
                         .execute();
    assert commit != null;
    Assert.assertEquals("test_message", commit.getFullMessage());
  }

  @Test
  public void amendCommitAuthor_theResultCommitShouldHaveTheInputAuthor() throws IOException {
    PersonIdent author = new PersonIdent("test_author_name", "test_author@email.com");
    RevCommit commit = Requests.commit(gfs)
                         .author(author)
                         .amend(true)
                         .execute();
    assert commit != null;
    Assert.assertEquals(author, commit.getAuthorIdent());
  }

  @Test
  public void amendCommitCommitter_theResultCommitShouldHaveTheInputCommitter() throws IOException {
    PersonIdent committer = new PersonIdent("test_committer_name", "test_committer@email.com");
    RevCommit commit = Requests.commit(gfs)
                         .committer(committer)
                         .amend(true)
                         .execute();
    assert commit != null;
    Assert.assertEquals(committer, commit.getCommitterIdent());
  }

}
