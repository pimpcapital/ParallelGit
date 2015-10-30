package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public final class TreeUtils {

  @Nonnull
  public static String normalizeTreePath(@Nonnull String path) {
    if(path.startsWith("/"))
      return path.substring(1);
    if(path.endsWith("/"))
      return path.substring(0, path.length() - 1);
    return path;
  }

  @Nonnull
  public static TreeWalk newTreeWalk(@Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    TreeWalk treeWalk = new TreeWalk(reader);
    treeWalk.reset(treeId);
    return treeWalk;
  }

  @Nonnull
  public static TreeWalk newTreeWalk(@Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    return newTreeWalk(treeId, repo.newObjectReader());
  }

  @Nullable
  public static TreeWalk forPath(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    return TreeWalk.forPath(reader, normalizeTreePath(path), treeId);
  }

  @Nullable
  public static TreeWalk forPath(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return forPath(path, treeId, reader);
    }
  }

  public static boolean exists(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    return forPath(path, treeId, reader) != null;
  }

  public static boolean exists(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return exists(path, treeId, reader);
    }
  }

  @Nullable
  public static AnyObjectId getObjectId(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getObjectId(0);
  }

  @Nullable
  public static AnyObjectId getObjectId(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, treeId, reader)) {
      return treeWalk != null ? getObjectId(treeWalk) : null;
    }
  }

  @Nullable
  public static AnyObjectId getObjectId(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getObjectId(path, treeId, reader);
    }
  }

  public static boolean isBlob(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB;
  }

  public static boolean isFileOrSymbolicLink(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, treeId, reader)) {
      return treeWalk != null && isBlob(treeWalk);
    }
  }

  public static boolean isFileOrSymbolicLink(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isFileOrSymbolicLink(path, treeId, reader);
    }
  }

  public static boolean isTree(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_TREE;
  }

  public static boolean isDirectory(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, treeId, reader)) {
      return treeWalk != null && isTree(treeWalk);
    }
  }

  public static boolean isDirectory(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isDirectory(path, treeId, reader);
    }
  }

  public static boolean isRegular(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.REGULAR_FILE;
  }

  public static boolean isRegularFile(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, treeId, reader)) {
      return treeWalk != null && isRegular(treeWalk);
    }
  }

  public static boolean isRegularFile(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isRegularFile(path, treeId, reader);
    }
  }

  public static boolean isExecutable(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.EXECUTABLE_FILE;
  }

  public static boolean isExecutableFile(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, treeId, reader)) {
      return treeWalk != null && isExecutable(treeWalk);
    }
  }

  public static boolean isExecutableFile(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isExecutableFile(path, treeId, reader);
    }
  }

  public static boolean isRegularOrExecutable(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.REGULAR_FILE || treeWalk.getFileMode(0) == FileMode.EXECUTABLE_FILE;
  }

  public static boolean isRegularOrExecutableFile(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, treeId, reader)) {
      return treeWalk != null && isRegularOrExecutable(treeWalk);
    }
  }

  public static boolean isRegularOrExecutableFile(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isRegularOrExecutableFile(path, treeId, reader);
    }
  }

  public static boolean isSymbolicLink(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.SYMLINK;
  }

  public static boolean isSymbolicLink(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, treeId, reader)) {
      return treeWalk != null && isSymbolicLink(treeWalk);
    }
  }

  public static boolean isSymbolicLink(@Nonnull String path, @Nonnull AnyObjectId treeId, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isSymbolicLink(path, treeId, reader);
    }
  }

}
