package com.beijunyi.parallelgit.filesystem;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.commands.GfsCheckout;
import com.beijunyi.parallelgit.filesystem.commands.GfsCommit;
import com.beijunyi.parallelgit.filesystem.commands.GfsMerge;
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
  public static GfsCheckout checkout(@Nonnull GitFileSystem gfs) {
    return new GfsCheckout(gfs);
  }

  @Nonnull
  public static GfsCommit commit(@Nonnull GitFileSystem gfs) {
    return new GfsCommit(gfs);
  }

  @Nonnull
  public static GfsMerge merge(@Nonnull GitFileSystem gfs) {
    return new GfsMerge(gfs);
  }

  public static void detach(@Nonnull GitFileSystem gfs) throws IOException {
    GfsStatusProvider status = gfs.getStatusProvider();
    if(status.isAttached()) {
      checkout(gfs).setTarget(status.commit().getName()).execute();
    }
  }

}
