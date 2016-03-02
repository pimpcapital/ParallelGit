package com.beijunyi.parallelgit.web.protocol.model;

import javax.annotation.Nonnull;

import org.eclipse.jgit.revwalk.RevCommit;

public class CommitView {

  private final String hash;
  private final String message;
  private final PersonView author;
  private final PersonView committer;

  private CommitView(@Nonnull String hash, @Nonnull String message, @Nonnull PersonView author, @Nonnull PersonView committer) {
    this.hash = hash;
    this.message = message;
    this.author = author;
    this.committer = committer;
  }

  @Nonnull
  public static CommitView of(@Nonnull RevCommit commit) {
    return new CommitView(commit.getName(), commit.getFullMessage(), PersonView.of(commit.getAuthorIdent()), PersonView.of(commit.getCommitterIdent()));
  }

  @Nonnull
  public String getHash() {
    return hash;
  }

  @Nonnull
  public String getMessage() {
    return message;
  }

  @Nonnull
  public PersonView getAuthor() {
    return author;
  }

  @Nonnull
  public PersonView getCommitter() {
    return committer;
  }

}
