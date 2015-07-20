package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public final class TreeWalkHelper {

  /**
   * Creates a new {@code TreeWalk} to walk through the given tree.
   *
   * @param   reader
   *          an object reader
   * @param   treeId
   *          the id of the tree to walk through
   * @return  a new {@code TreeWalk} to walk through the given tree.
   */
  @Nonnull
  public static TreeWalk newTreeWalk(@Nonnull ObjectReader reader, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = new TreeWalk(reader);
    treeWalk.reset(treeId);
    return treeWalk;
  }

  /**
   * Creates a new {@code TreeWalk} to walk through the given tree.
   *
   * @param   repo
   *          a git repository
   * @param   treeId
   *          the id the tree to walk through
   * @return  a new {@code TreeWalk} to walk through the given tree.
   */
  @Nonnull
  public static TreeWalk newTreeWalk(@Nonnull Repository repo, @Nonnull AnyObjectId treeId) throws IOException {
    return newTreeWalk(repo.newObjectReader(), treeId);
  }

  /**
   * Tests if the given path exists in the given tree.
   *
   * @param   reader
   *          an object reader
   * @param   path
   *          the path to test
   * @param   treeId
   *          the id of a file tree
   * @return  {@code true} if the given path exists in the given tree.
   */
  public static boolean exists(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    return TreeWalk.forPath(reader, path, treeId) != null;
  }

  /**
   * Tests if the given path exists in the given tree.
   *
   * @param   repo
   *          a git repository
   * @param   path
   *          the path to test
   * @param   treeId
   *          the id a file tree
   * @return  {@code true} if the given path exists in the given tree.
   */
  public static boolean exists(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    return TreeWalk.forPath(repo, path, treeId) != null;
  }

  /**
   * Gets the first attached object of the given {@code TreeWalk} entry.
   *
   * @param   treeWalk
   *          an open {@code TreeWalk}
   * @return  the first attached object of the current node.
   */
  @Nullable
  public static ObjectId getObject(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getObjectId(0);
  }

  /**
   * Gets first attached object of the entry at the given path within the given tree.
   *
   * @param   reader
   *          an object reader
   * @param   path
   *          a file path
   * @param   treeId
   *          the id of a file tree
   * @return  the first attached object of the entry at the given path within the given tree.
   */
  @Nullable
  public static ObjectId getObject(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId);
    if(treeWalk == null)
      return null;
    try {
      return getObject(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Gets first attached object of the entry at the given path within the given tree.
   *
   * @param   repo
   *          a git repository
   * @param   path
   *          a file path
   * @param   treeId
   *          the id of a file tree
   * @return  the first attached object of the entry at the given path within the given tree.
   */
  @Nullable
  public static ObjectId getObject(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId);
    if(treeWalk == null)
      return null;
    try {
      return getObject(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a tree walk entry is a regular file.
   *
   * @param   treeWalk
   *          the tree walk entry to test
   * @return  {@code true} if the given tree walk entry entry is a regular file
   */
  public static boolean isFile(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB;
  }

  /**
   * Tests if a file is a regular file.
   *
   * @param   reader
   *          an object reader
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if the specified file is a regular file
   */
  public static boolean isFile(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isFile(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a file is a regular file.
   *
   * @param   repo
   *          a git repository
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if the specified file is a regular file
   */
  public static boolean isFile(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isFile(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a tree walk entry is a directory.
   *
   * @param   treeWalk
   *          the tree walk entry to test
   * @return  {@code true} if the given tree walk entry entry is a directory
   */
  public static boolean isDirectory(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_TREE;
  }

  /**
   * Tests if a file is a directory.
   *
   * @param   reader
   *          an object reader
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if the specified file is a directory
   */
  public static boolean isDirectory(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isDirectory(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a file is a directory.
   *
   * @param   repo
   *          a git repository
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if the specified file is a directory
   */
  public static boolean isDirectory(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isDirectory(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a tree walk entry is executable.
   *
   * @param   treeWalk
   *          the tree walk entry to test
   * @return  {@code true} if the given tree walk entry entry is executable
   */
  public static boolean isExecutable(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.EXECUTABLE_FILE;
  }

  /**
   * Tests if a file is executable.
   *
   * @param   reader
   *          an object reader
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if the specified file is executable
   */
  public static boolean isExecutable(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isExecutable(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a file is executable.
   *
   * @param   repo
   *          a git repository
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if the specified file is executable
   */
  public static boolean isExecutable(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isExecutable(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

}
