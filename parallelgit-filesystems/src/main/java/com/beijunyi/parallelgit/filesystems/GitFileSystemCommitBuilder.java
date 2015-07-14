package com.beijunyi.parallelgit.filesystems;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.PersonIdent;

public class GitFileSystemCommitBuilder {

  private GitFileStore store;
  private PersonIdent author;
  private String authorName;
  private String authorEmail;
  private PersonIdent committer;
  private String committerName;
  private String committerEmail;

  @Nonnull
  static GitFileSystemCommitBuilder prepare() {
    return new GitFileSystemCommitBuilder();
  }
}
