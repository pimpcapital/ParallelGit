package com.beijunyi.parallelgit.filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.requests.CommitRequest;
import com.beijunyi.parallelgit.filesystem.requests.MergeRequest;
import com.beijunyi.parallelgit.filesystem.utils.GfsBuilder;
import com.beijunyi.parallelgit.filesystem.utils.GfsParams;
import com.beijunyi.parallelgit.filesystem.utils.GfsUriUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;

public final class Gfs {

  @Nonnull
  public static GfsBuilder newFileSystem(@Nonnull Repository repo) {
    return new GfsBuilder(repo);
  }

  @Nonnull
  public static GfsBuilder newFileSystem(@Nonnull File repoDir) throws IOException {
    return new GfsBuilder(repoDir);
  }

  @Nonnull
  public static GfsBuilder newFileSystem(@Nonnull String repoDir) throws IOException {
    return new GfsBuilder(repoDir);
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull String branch, @Nonnull Repository repo) throws IOException {
    return newFileSystem(repo)
             .branch(branch)
             .build();
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull String branch, @Nonnull File repoDir) throws IOException {
    return newFileSystem(repoDir)
             .branch(branch)
             .build();
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull String branch, @Nonnull String repoDir) throws IOException {
    return newFileSystem(repoDir)
             .branch(branch)
             .build();
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull AnyObjectId commit, @Nonnull Repository repo) throws IOException {
    return newFileSystem(repo)
             .commit(commit)
             .build();
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull URI uri, @Nonnull Map<String, ?> properties) throws IOException {
    return newFileSystem(GfsUriUtils.getRepository(uri))
             .readParams(GfsParams.fromProperties(properties))
             .build();
  }

  @Nonnull
  public static GitFileSystem newFileSystem(@Nonnull Path path, @Nonnull Map<String, ?> properties) throws IOException {
    return newFileSystem(path.toFile())
             .readParams(GfsParams.fromProperties(properties))
             .build();
  }

  @Nonnull
  public static CommitRequest commit(@Nonnull GitFileSystem gfs) {
    return new CommitRequest(gfs);
  }

  @Nonnull
  public static MergeRequest merge(@Nonnull GitFileSystem gfs) {
    return new MergeRequest(gfs);
  }

}
