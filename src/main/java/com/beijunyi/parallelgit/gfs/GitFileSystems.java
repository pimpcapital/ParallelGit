package com.beijunyi.parallelgit.gfs;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.RepositoryHelper;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

/**
 * Factory methods for {@code GitFileSystem}s.
 */
public class GitFileSystems {

  @Nullable
  public static GitFileSystem getFileSystem(@Nonnull String sessionId) {
    return GitFileSystemProvider.getInstance().getFileSystem(sessionId);
  }

  @Nullable
  public static GitFileSystem getFileSystem(@Nonnull URI uri) {
    return GitFileSystemProvider.getInstance().getFileSystem(uri);
  }

  @Nullable
  public static GitFileSystem newFileSystem(@Nonnull URI uri) {
    return GitFileSystemProvider.getInstance().newFileSystem(uri, null);
  }

  @Nullable
  public static GitFileSystem newFileSystem(@Nonnull URI uri, @Nullable Map<String,?> env) {
    return GitFileSystemProvider.getInstance().newFileSystem(uri, env);
  }


  @Nullable
  public static GitFileSystem newFileSystem(@Nonnull Path uri) {
    return GitFileSystemProvider.getInstance().newFileSystem(uri, null);
  }

  @Nullable
  public static GitFileSystem newFileSystem(@Nonnull Path path, @Nullable Map<String,?> env) {
    return GitFileSystemProvider.getInstance().newFileSystem(path, env);
  }

  @Nullable
  public static GitFileSystem newFileSystem(@Nonnull File repoDir) {
    return GitFileSystemProvider.getInstance().newFileSystem(repoDir, null);
  }

  @Nullable
  public static GitFileSystem newFileSystem(@Nonnull File repoDir, @Nullable Map<String,?> env) {
    return GitFileSystemProvider.getInstance().newFileSystem(repoDir, env);
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull Repository repo) {
    return GitFileSystemProvider.getInstance().newFileSystem(null, repo, null, null, null);
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull Repository repo, @Nullable ObjectId revision) {
    return GitFileSystemProvider.getInstance().newFileSystem(null, repo, null, revision, null);
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull Repository repo, @Nullable String branchRef) {
    return GitFileSystemProvider.getInstance().newFileSystem(null, repo, branchRef, null, null);
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull Repository repo, @Nullable String branchRef, @Nullable ObjectId revision) {
    return GitFileSystemProvider.getInstance().newFileSystem(null, repo, branchRef, revision, null);
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull Repository repo, @Nullable String branchRef, @Nullable ObjectId revision, @Nullable ObjectId tree) {
    return GitFileSystemProvider.getInstance().newFileSystem(null, repo, branchRef, revision, tree);
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nullable String sessionId, @Nonnull Repository repo, @Nullable String branch, @Nullable String revisionStr) {
    ObjectId revision = revisionStr != null ? RepositoryHelper.getRevisionId(repo, revisionStr) : null;
    return GitFileSystemProvider.getInstance().newFileSystem(sessionId, repo, branch, revision, null);
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nullable String sessionId, @Nonnull Repository repo, @Nullable String branchRef, @Nullable ObjectId revision, @Nullable ObjectId tree) {
    return GitFileSystemProvider.getInstance().newFileSystem(sessionId, repo, branchRef, revision, tree);
  }

}
