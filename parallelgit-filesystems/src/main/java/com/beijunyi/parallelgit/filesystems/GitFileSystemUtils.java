package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * General {@code GitFileSystem} manipulation utilities.
 */
public final class GitFileSystemUtils {

  public static void fastDeleteDirectory(@Nonnull Path path) throws IOException {
    GitPath gitPath = (GitPath) path;
    gitPath.getFileSystem().getFileStore().fastDeleteDirectory(gitPath.getNormalizedString());
  }

  @Nullable
  public static ObjectId writeTree(@Nonnull FileStore store) throws IOException {
    return ((GitFileStore) store).writeAndUpdateTree();
  }

  @Nullable
  public static ObjectId writeTree(@Nonnull FileSystem fs) throws IOException {
    return writeTree(((GitFileSystem) fs).getFileStore());
  }

  @Nullable
  public static ObjectId writeTree(@Nonnull Path path) throws IOException {
    return writeTree(((GitPath) path).getFileSystem());
  }

  @Nullable
  public static RevCommit commit(@Nonnull FileStore store, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, boolean amend) throws IOException {
    return ((GitFileStore) store).writeAndUpdateCommit(author, committer, message, amend);
  }

  @Nullable
  public static RevCommit commit(@Nonnull FileSystem fs, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, boolean amend) throws IOException {
    return commit(((GitFileSystem) fs).getFileStore(), author, committer, message, amend);
  }

  @Nullable
  public static RevCommit commit(@Nonnull Path path, @Nonnull PersonIdent author, @Nonnull PersonIdent committer, @Nonnull String message, boolean amend) throws IOException {
    return commit(((GitPath) path).getFileSystem(), author, committer, message, amend);
  }

  @Nullable
  public static RevCommit commit(@Nonnull Path path, @Nonnull PersonIdent author, @Nonnull String message, boolean amend) throws IOException {
    return commit(path, author, author, message, amend);
  }

  @Nullable
  public static RevCommit commit(@Nonnull Path path, @Nonnull String authorName, @Nonnull String authorEmail, @Nonnull String message, boolean amend) throws IOException {
    return commit(path, new PersonIdent(authorName, authorEmail), message, amend);
  }

  @Nullable
  public static RevCommit commit(@Nonnull Path path, @Nonnull String authorName, @Nonnull String authorEmail, @Nonnull String message) throws IOException {
    return commit(path, authorName, authorEmail, message, false);
  }

  @Nonnull
  public static Repository getRepository(@Nonnull FileStore store) {
    return ((GitFileStore) store).getRepository();
  }

  @Nonnull
  public static Repository getRepository(@Nonnull FileSystem fs) {
    return getRepository(((GitFileSystem) fs).getFileStore());
  }

  @Nonnull
  public static Repository getRepository(@Nonnull Path path) {
    return getRepository(((GitPath) path).getFileSystem());
  }

  @Nullable
  public static String getBranch(@Nonnull FileStore store) {
    return ((GitFileStore) store).getBranch();
  }

  @Nullable
  public static String getBranch(@Nonnull FileSystem fs) {
    return getBranch(((GitFileSystem) fs).getFileStore());
  }

  @Nullable
  public static String getBranch(@Nonnull Path path) {
    return getBranch(((GitPath) path).getFileSystem());
  }

  @Nullable
  public static RevCommit getBaseCommit(@Nonnull FileStore store) {
    return ((GitFileStore) store).getBaseCommit();
  }

  @Nullable
  public static RevCommit getBaseCommit(@Nonnull FileSystem fs) {
    return getBaseCommit(((GitFileSystem) fs).getFileStore());
  }

  @Nullable
  public static RevCommit getBaseCommit(@Nonnull Path path) {
    return getBaseCommit(((GitPath) path).getFileSystem());
  }

  @Nullable
  public static AnyObjectId getBaseTree(@Nonnull FileStore store) {
    return ((GitFileStore) store).getBaseTree();
  }

  @Nullable
  public static AnyObjectId getBaseTree(@Nonnull FileSystem fs) {
    return getBaseTree(((GitFileSystem) fs).getFileStore());
  }

  @Nullable
  public static AnyObjectId getBaseTree(@Nonnull Path path) {
    return getBaseTree(((GitPath) path).getFileSystem());
  }

}
