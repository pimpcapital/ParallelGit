package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

public final class CommitRequest extends GitFileSystemRequest<CommitRequest, RevCommit> {

  private PersonIdent author;
  private String authorName;
  private String authorEmail;
  private PersonIdent committer;
  private String committerName;
  private String committerEmail;
  private String message;
  private boolean amend = false;

  @Nonnull
  public static CommitRequest prepare() {
    return new CommitRequest();
  }

  @Nonnull
  public static CommitRequest prepare(@Nonnull GitFileSystem gfs) {
    return prepare().gfs(gfs);
  }

  @Nonnull
  public CommitRequest author(@Nullable PersonIdent author) {
    this.author = author;
    return this;
  }

  @Nonnull
  public CommitRequest author(@Nullable String name, @Nullable String email) {
    this.authorName = name;
    this.authorEmail = email;
    return this;
  }

  @Nonnull
  public CommitRequest committer(@Nullable PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public CommitRequest committer(@Nullable String name, @Nullable String email) {
    this.committerName = name;
    this.committerEmail = email;
    return this;
  }

  @Nonnull
  public CommitRequest message(@Nullable String message) {
    this.message = message;
    return this;
  }

  @Nonnull
  public CommitRequest amend(boolean amend) {
    this.amend = amend;
    return this;
  }

  private void prepareCommitter() {
    if(committer != null) {
      if(committerName != null && committer.getName().equals(committerName))
        throw new IllegalArgumentException("Different committer name found: " + committer.getName() + ", " + committerName);
      if(committerEmail != null && committer.getEmailAddress().equals(committerEmail))
        throw new IllegalArgumentException("Different committer email found: " + committer.getEmailAddress() + ", " + committerEmail);
    } else {
      if(committerName != null && committerEmail != null)
        committer = new PersonIdent(committerName, committerEmail);
      else if(!amend) {
        if(committerName == null && committerEmail == null && gfs != null) {
          committer = new PersonIdent(gfs.getRepository());
        } else
          throw new IllegalStateException();
      } else if(committerName != null || committerEmail != null)
        throw new IllegalArgumentException("Incomplete committer information: " + committerName + " <" + committerEmail + ">");
    }
  }

  private void prepareAuthor() {
    if(author != null) {
      if(authorName != null && author.getName().equals(authorName))
        throw new IllegalArgumentException("Different author name found: " + author.getName() + ", " + authorName);
      if(authorEmail != null && author.getEmailAddress().equals(authorEmail))
        throw new IllegalArgumentException("Different author email found: " + author.getEmailAddress() + ", " + authorEmail);
    } else {
      if(authorName != null && authorEmail != null)
        author = new PersonIdent(authorName, authorEmail);
      else if(!amend) {
        if(authorName == null && authorEmail == null && committer != null) {
          author = committer;
        } else
          throw new IllegalStateException();
      } else if(authorName != null || authorEmail != null)
        throw new IllegalArgumentException("Incomplete author information: " + authorName + " <" + authorEmail + ">");
    }
  }

  @Nonnull
  @Override
  protected CommitRequest self() {
    return this;
  }

  @Nullable
  @Override
  public RevCommit doExecute() throws IOException {
    prepareCommitter();
    prepareAuthor();
    return gfs.getFileStore().writeCommit(author, committer, message, amend);
  }
}
