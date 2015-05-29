package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.ProviderMismatchException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.Repository;

/**
 * Factory methods for {@code GitPath}s.
 */
public class GitPaths {

  /**
   * Creates {@link GitPath} from the specified {@link URI}.
   *
   * @param uri a uri
   * @return a git path
   * @throws ProviderMismatchException when the scheme part of the {@code URI} is not "git"
   */
  @Nonnull
  public static GitPath get(@Nonnull URI uri) throws ProviderMismatchException {
    return GitFileSystemProvider.getInstance().getPath(uri);
  }

  /**
   * Creates {@link GitPath} from the specified {@link Repository}, {@code revision} and the {@code file path} within
   * the repository.
   *
   * @param repo a git repository
   * @param revision a revision id in the form of a string
   * @param fileInRepo a path that points to a file within the repository
   * @param more additional strings to be joined to form the path string
   * @return a git path
   */
  @Nonnull
  public static GitPath get(@Nonnull Repository repo, @Nullable String revision, @Nonnull String fileInRepo, @Nonnull String... more) throws IOException {
    return GitFileSystems.newFileSystem(null, repo, null, revision).getPath(fileInRepo, more).toAbsolutePath();
  }

  /**
   * Creates {@link GitPath} from the specified {@link Repository}, {@code revision} and the {@code file path} within
   * the repository.
   *
   * @param repo a git repository
   * @param fileInRepo a path that points to a file within the repository
   * @return a git path
   */
  @Nonnull
  public static GitPath get(@Nonnull Repository repo, @Nonnull String fileInRepo) throws IOException {
    return get(repo, null, fileInRepo);
  }

}
