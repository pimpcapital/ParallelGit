package com.beijunyi.parallelgit.filesystem;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.commands.GfsCheckoutCommand;
import com.beijunyi.parallelgit.filesystem.commands.GfsCommitCommand;
import com.beijunyi.parallelgit.filesystem.commands.GfsMergeCommand;
import com.beijunyi.parallelgit.filesystem.utils.GfsConfiguration;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;

import static com.beijunyi.parallelgit.filesystem.utils.GfsConfiguration.*;

public final class Gfs {

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull GfsConfiguration cfg) throws IOException {
    return GitFileSystemProvider.getInstance().newFileSystem(cfg);
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull Repository repo) throws IOException {
    return newFileSystem(repo(repo));
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull File repoDir) throws IOException {
    return newFileSystem(fileRepo(repoDir));
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull String repoDir) throws IOException {
    return newFileSystem(fileRepo(repoDir));
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull String branch, @Nonnull Repository repo) throws IOException {
    return newFileSystem(repo(repo).branch(branch));
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull String branch, @Nonnull File repoDir) throws IOException {
    return newFileSystem(fileRepo(repoDir).branch(branch));

  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull String branch, @Nonnull String repoDir) throws IOException {
    return newFileSystem(fileRepo(repoDir).branch(branch));
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    return newFileSystem(repo(repo).commit(commit));
  }

  @Nonnull
  public static GfsCheckoutCommand checkout(@Nonnull GitFileSystem gfs) {
    return new GfsCheckoutCommand(gfs);
  }

  @Nonnull
  public static GfsCommitCommand commit(@Nonnull GitFileSystem gfs) {
    return new GfsCommitCommand(gfs);
  }

  @Nonnull
  public static GfsMergeCommand merge(@Nonnull GitFileSystem gfs) {
    return new GfsMergeCommand(gfs);
  }

}
