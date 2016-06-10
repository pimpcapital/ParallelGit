package com.beijunyi.parallelgit.filesystem;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.commands.*;
import com.beijunyi.parallelgit.filesystem.utils.GfsConfiguration;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;

import static com.beijunyi.parallelgit.filesystem.utils.GfsConfiguration.*;

public final class Gfs {

  @Nonnull
  public static GitFileSystem newFileSystem(GfsConfiguration cfg) throws IOException {
    return GitFileSystemProvider.getInstance().newFileSystem(cfg);
  }

  @Nonnull
  public static GitFileSystem newFileSystem(Repository repo) throws IOException {
    return newFileSystem(repo(repo));
  }

  @Nonnull
  public static GitFileSystem newFileSystem(File repoDir) throws IOException {
    return newFileSystem(fileRepo(repoDir));
  }

  @Nonnull
  public static GitFileSystem newFileSystem(String repoDir) throws IOException {
    return newFileSystem(fileRepo(repoDir));
  }

  @Nonnull
  public static GitFileSystem newFileSystem(String branch, Repository repo) throws IOException {
    return newFileSystem(repo(repo).branch(branch));
  }

  @Nonnull
  public static GitFileSystem newFileSystem(String branch, File repoDir) throws IOException {
    return newFileSystem(fileRepo(repoDir).branch(branch));

  }

  @Nonnull
  public static GitFileSystem newFileSystem(String branch, String repoDir) throws IOException {
    return newFileSystem(fileRepo(repoDir).branch(branch));
  }

  @Nonnull
  public static GitFileSystem newFileSystem(AnyObjectId commit, Repository repo) throws IOException {
    return newFileSystem(repo(repo).commit(commit));
  }

  @Nonnull
  public static GfsCheckout checkout(GitFileSystem gfs) {
    return new GfsCheckout(gfs);
  }

  @Nonnull
  public static GfsCommit commit(GitFileSystem gfs) {
    return new GfsCommit(gfs);
  }

  @Nonnull
  public static GfsMerge merge(GitFileSystem gfs) {
    return new GfsMerge(gfs);
  }

  @Nonnull
  public static GfsReset reset(GitFileSystem gfs) {
    return new GfsReset(gfs);
  }

  @Nonnull
  public static GfsCreateStash stash(GitFileSystem gfs) {
    return new GfsCreateStash(gfs);
  }

  public static boolean isDirty(GitFileSystem gfs) throws IOException {
    return gfs.getFileStore().getRoot().isModified();
  }

  public static void detach(GitFileSystem gfs) throws IOException {
    GfsStatusProvider status = gfs.getStatusProvider();
    if(status.isAttached()) {
      checkout(gfs).target(status.commit().getName()).execute();
    }
  }

}
