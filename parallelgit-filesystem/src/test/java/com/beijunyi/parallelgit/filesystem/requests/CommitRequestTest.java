package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.utils.*;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
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
    Assert.assertEquals(RevTreeHelper.getRootTree(repo, commit.getParent(0)), commit.getTree());
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
  public void commitWithAuthorNameAndEmail_theResultCommitShouldHaveTheInputAuthorNameAndEmail() throws IOException {
    writeSomeFileToGfs();
    RevCommit commit = Requests.commit(gfs)
                         .author("test_author_name", "test_author@email.com")
                         .execute();
    assert commit != null;
    Assert.assertEquals("test_author_name", commit.getAuthorIdent().getName());
    Assert.assertEquals("test_author@email.com", commit.getAuthorIdent().getEmailAddress());
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
  public void commitWithCommitterNameAndEmail_theResultCommitShouldHaveTheInputCommitterNameAndEmail() throws IOException {
    writeSomeFileToGfs();
    RevCommit commit = Requests.commit(gfs)
                         .committer("test_committer_name", "test_committer@email.com")
                         .execute();
    assert commit != null;
    Assert.assertEquals("test_committer_name", commit.getCommitterIdent().getName());
    Assert.assertEquals("test_committer@email.com", commit.getCommitterIdent().getEmailAddress());
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
  public void commitWithParent_theResultCommitShouldHaveTheSpecifiedParent() throws IOException {
    writeSomeFileToGfs();
    DirCache cache = DirCache.newInCore();
    AnyObjectId blobId = BlobHelper.insert(repo, "some other content");
    CacheHelper.addFile(cache, "some_other_file.txt", blobId);
    AnyObjectId parent = CommitHelper.createCommit(repo, cache, new PersonIdent(repo), "some orphan commit");
    RevCommit commit = Requests.commit(gfs)
                         .parent(parent)
                         .execute();
    assert commit != null;
    Assert.assertEquals(parent, commit.getParent(0));
  }

  @Test
  public void commitWithMultipleParents_theResultCommitShouldHaveTheSpecifiedParents() throws IOException {
    writeSomeFileToGfs();
    DirCache cache = DirCache.newInCore();
    AnyObjectId blobId = BlobHelper.insert(repo, "some other content");
    CacheHelper.addFile(cache, "some_other_file.txt", blobId);
    AnyObjectId secondParent = CommitHelper.createCommit(repo, cache, new PersonIdent(repo), "some orphan commit");
    AnyObjectId[] parents = new AnyObjectId[] {CommitHelper.getCommit(repo, branch), secondParent};
    RevCommit commit = Requests.commit(gfs)
                         .parents(Arrays.asList(parents))
                         .execute();
    assert commit != null;
    Assert.assertArrayEquals(parents, commit.getParents());
  }

  @Test
  public void commitWithNoParent_theResultCommitShouldHaveNoParent() throws IOException {
    writeSomeFileToGfs();
    RevCommit commit = Requests.commit(gfs)
                         .parents(Collections.<AnyObjectId>emptyList())
                         .execute();
    assert commit != null;
    Assert.assertEquals(0, commit.getParentCount());
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
