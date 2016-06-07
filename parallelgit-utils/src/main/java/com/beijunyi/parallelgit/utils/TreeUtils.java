package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public final class TreeUtils {

  @Nonnull
  public static String normalizeTreePath(String path) {
    if(path.startsWith("/"))
      return path.substring(1);
    if(path.endsWith("/"))
      return path.substring(0, path.length() - 1);
    return path;
  }

  @Nonnull
  public static TreeWalk newTreeWalk(AnyObjectId tree, ObjectReader reader) throws IOException {
    TreeWalk treeWalk = new TreeWalk(reader);
    treeWalk.reset(tree);
    return treeWalk;
  }

  @Nonnull
  public static TreeWalk newTreeWalk(AnyObjectId tree, Repository repo) throws IOException {
    return newTreeWalk(tree, repo.newObjectReader());
  }

  @Nullable
  public static TreeWalk forPath(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    return TreeWalk.forPath(reader, normalizeTreePath(path), tree);
  }

  @Nullable
  public static TreeWalk forPath(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return forPath(path, tree, reader);
    }
  }

  public static boolean exists(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    return forPath(path, tree, reader) != null;
  }

  public static boolean exists(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return exists(path, tree, reader);
    }
  }

  @Nullable
  public static ObjectId getObjectId(TreeWalk treeWalk) {
    return treeWalk.getObjectId(0);
  }

  @Nullable
  public static ObjectId getObjectId(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, tree, reader)) {
      return treeWalk != null ? getObjectId(treeWalk) : null;
    }
  }

  @Nullable
  public static ObjectId getObjectId(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getObjectId(path, tree, reader);
    }
  }

  @Nonnull
  public static InputStream openFile(String file, AnyObjectId tree, ObjectReader reader) throws IOException {
    AnyObjectId blobId = getObjectId(file, tree, reader);
    if(blobId == null)
      throw new NoSuchFileException(file);
    return ObjectUtils.openBlob(blobId, reader);
  }

  @Nonnull
  public static InputStream openFile(String file, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return openFile(file, tree, reader);
    }
  }

  @Nonnull
  public static BlobSnapshot readFile(String file, ObjectId tree, ObjectReader reader) throws IOException {
    ObjectId blobId = getObjectId(file, tree, reader);
    if(blobId == null)
      throw new NoSuchFileException(file);
    return ObjectUtils.readBlob(blobId, reader);
  }

  @Nonnull
  public static BlobSnapshot readFile(String file, ObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readFile(file, tree, reader);
    }
  }

  @Nonnull
  public static TreeSnapshot readDirectory(String dir, ObjectId tree, ObjectReader reader) throws IOException {
    ObjectId blobId = getObjectId(dir, tree, reader);
    if(blobId == null)
      throw new NotDirectoryException(dir);
    return ObjectUtils.readTree(blobId, reader);
  }

  @Nonnull
  public static TreeSnapshot readDirectory(String dir, ObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readDirectory(dir, tree, reader);
    }
  }

  public static boolean isBlob(TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB;
  }

  public static boolean isFileOrSymbolicLink(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, tree, reader)) {
      return treeWalk != null && isBlob(treeWalk);
    }
  }

  public static boolean isFileOrSymbolicLink(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isFileOrSymbolicLink(path, tree, reader);
    }
  }

  public static boolean isTree(TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_TREE;
  }

  public static boolean isDirectory(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, tree, reader)) {
      return treeWalk != null && isTree(treeWalk);
    }
  }

  public static boolean isDirectory(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isDirectory(path, tree, reader);
    }
  }

  public static boolean isRegular(TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.REGULAR_FILE;
  }

  public static boolean isRegularFile(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, tree, reader)) {
      return treeWalk != null && isRegular(treeWalk);
    }
  }

  public static boolean isRegularFile(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isRegularFile(path, tree, reader);
    }
  }

  public static boolean isExecutable(TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.EXECUTABLE_FILE;
  }

  public static boolean isExecutableFile(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, tree, reader)) {
      return treeWalk != null && isExecutable(treeWalk);
    }
  }

  public static boolean isExecutableFile(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isExecutableFile(path, tree, reader);
    }
  }

  public static boolean isRegularOrExecutable(TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.REGULAR_FILE || treeWalk.getFileMode(0) == FileMode.EXECUTABLE_FILE;
  }

  public static boolean isRegularOrExecutableFile(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, tree, reader)) {
      return treeWalk != null && isRegularOrExecutable(treeWalk);
    }
  }

  public static boolean isRegularOrExecutableFile(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isRegularOrExecutableFile(path, tree, reader);
    }
  }

  public static boolean isSymbolicLink(TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.SYMLINK;
  }

  public static boolean isSymbolicLink(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk treeWalk = forPath(path, tree, reader)) {
      return treeWalk != null && isSymbolicLink(treeWalk);
    }
  }

  public static boolean isSymbolicLink(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isSymbolicLink(path, tree, reader);
    }
  }

}
