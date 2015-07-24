package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileStore;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.GitPath;
import org.eclipse.jgit.lib.ObjectId;

/**
 * General {@code GitFileSystem} manipulation utilities.
 */
public final class GitFileSystemUtils {

  public static void deleteDirectory(@Nonnull GitPath path) throws IOException {
    path.getFileSystem().getFileStore().deleteRecursively(path);
  }

  @Nullable
  public static ObjectId writeTree(@Nonnull FileStore store) throws IOException {
    return ((GitFileStore) store).persistChanges();
  }

  @Nullable
  public static ObjectId writeTree(@Nonnull FileSystem fs) throws IOException {
    return writeTree(((GitFileSystem) fs).getFileStore());
  }

  @Nullable
  public static ObjectId writeTree(@Nonnull Path path) throws IOException {
    return writeTree(((GitPath)path).getFileSystem());
  }

}
