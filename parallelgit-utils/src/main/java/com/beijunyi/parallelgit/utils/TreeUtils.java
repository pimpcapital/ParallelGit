package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public final class TreeUtils {

  @Nonnull
  public static TreeWalk newTreeWalk(@Nonnull ObjectReader reader, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = new TreeWalk(reader);
    treeWalk.reset(treeId);
    return treeWalk;
  }

  @Nonnull
  public static TreeWalk newTreeWalk(@Nonnull Repository repo, @Nonnull AnyObjectId treeId) throws IOException {
    return newTreeWalk(repo.newObjectReader(), treeId);
  }

  public static boolean exists(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    return TreeWalk.forPath(reader, path, treeId) != null;
  }

  public static boolean exists(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    return TreeWalk.forPath(repo, path, treeId) != null;
  }

  @Nullable
  public static AnyObjectId getObjectId(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getObjectId(0);
  }

  @Nullable
  public static AnyObjectId getObjectId(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId)) {
      return treeWalk != null ? getObjectId(treeWalk) : null;
    }
  }

  @Nullable
  public static AnyObjectId getObjectId(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId)) {
      return treeWalk != null ? getObjectId(treeWalk) : null;
    }
  }

  public static boolean isBlob(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB;
  }

  public static boolean isFileOrSymbolicLink(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId)) {
      return treeWalk != null && isBlob(treeWalk);
    }
  }

  public static boolean isFileOrSymbolicLink(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId)) {
      return treeWalk != null && isBlob(treeWalk);
    }
  }

  public static boolean isTree(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_TREE;
  }

  public static boolean isDirectory(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId)) {
      return treeWalk != null && isTree(treeWalk);
    }
  }

  public static boolean isDirectory(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId)) {
      return treeWalk != null && isTree(treeWalk);
    }
  }

  public static boolean isRegular(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.REGULAR_FILE;
  }

  public static boolean isRegularFile(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId)) {
      return treeWalk != null && isRegular(treeWalk);
    }
  }

  public static boolean isRegularFile(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId)) {
      return treeWalk != null && isRegular(treeWalk);
    }
  }

  public static boolean isExecutable(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.EXECUTABLE_FILE;
  }

  public static boolean isExecutableFile(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId)) {
      return treeWalk != null && isExecutable(treeWalk);
    }
  }

  public static boolean isExecutableFile(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId)) {
      return treeWalk != null && isExecutable(treeWalk);
    }
  }

  public static boolean isRegularOrExecutable(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.REGULAR_FILE || treeWalk.getFileMode(0) == FileMode.EXECUTABLE_FILE;
  }

  public static boolean isRegularOrExecutableFile(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId)) {
      return treeWalk != null && isRegularOrExecutable(treeWalk);
    }
  }

  public static boolean isRegularOrExecutableFile(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId)) {
      return treeWalk != null && isRegularOrExecutable(treeWalk);
    }
  }

  public static boolean isSymbolicLink(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.SYMLINK;
  }

  public static boolean isSymbolicLink(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId)) {
      return treeWalk != null && isSymbolicLink(treeWalk);
    }
  }

  public static boolean isSymbolicLink(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    try(TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId)) {
      return treeWalk != null && isSymbolicLink(treeWalk);
    }
  }

}
