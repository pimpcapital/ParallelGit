package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public class TreeWalkHelper {

  /**
   * Creates a new {@code TreeWalk} to run over the given tree.
   *
   * @param reader an object reader
   * @param treeId a tree id
   * @return a new tree walk to run over the given tree.
   */
  @Nonnull
  public static TreeWalk newTreeWalk(@Nonnull ObjectReader reader, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = new TreeWalk(reader);
    treeWalk.reset(treeId);
    return treeWalk;
  }

  /**
   * Creates a new {@code TreeWalk} of the given tree.
   *
   * @param repo a git repository
   * @param treeId a tree id
   * @return a new tree walk of the given tree.
   */
  @Nonnull
  public static TreeWalk newTreeWalk(@Nonnull Repository repo, @Nonnull AnyObjectId treeId) throws IOException {
    return newTreeWalk(repo.newObjectReader(), treeId);
  }

  public static boolean exists(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    return TreeWalk.forPath(reader, path, treeId) != null;
  }

  /**
   * Tests if a node (either file or directory) exists at the specified path.
   *
   * @param repo a git repository
   * @param path a path
   * @param treeId a tree id
   * @return {@code true} if a node exists at the specified path
   */
  public static boolean exists(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    return TreeWalk.forPath(repo, path, treeId) != null;
  }

  /**
   * Gets the first object id of the current node.
   *
   * @param treeWalk a tree walk
   * @return the first object id of the current node
   */
  @Nullable
  public static ObjectId getObjectId(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getObjectId(0);
  }

  /**
   * Gets the object id of the node at the specified path.
   *
   * @param reader an object reader
   * @param path a file path
   * @param treeId a tree id
   * @return the object id of the node at the specified path
   */
  @Nullable
  public static ObjectId getObjectId(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId);
    if(treeWalk == null)
      return null;
    try {
      return getObjectId(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Gets the object id of the node at the specified path.
   *
   * @param repo a git repository
   * @param path a file path
   * @param treeId a tree id
   * @return the object id of the node at the specified path
   */
  @Nullable
  public static ObjectId getObjectId(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId);
    if(treeWalk == null)
      return null;
    try {
      return getObjectId(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if the current node of the given {@link TreeWalk} is a blob object.
   *
   * @param treeWalk a tree walk
   * @return {@code true} if the current node is a blob object
   */
  public static boolean isFile(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB;
  }

  /**
   * Tests if the specified path points to a file in the given tree.
   *
   * @param reader an object reader
   * @param path a file path
   * @param treeId a tree id
   * @return {@code true} if the specified path points to a file
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
   * Tests if the specified path points to a file in the given tree.
   *
   * @param repo a git repository
   * @param path a file path
   * @param treeId a tree id
   * @return {@code true} if the specified path points to a file
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
   * Tests if the current node of the given {@link TreeWalk} is a tree object.
   *
   * @param treeWalk a tree walk
   * @return {@code true} if the current node is a tree object
   */
  public static boolean isDirectory(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_TREE;
  }

  /**
   * Tests if the specified path points to a directory in the given tree.
   *
   * @param reader an object reader
   * @param path a file path
   * @param treeId a tree id
   * @return {@code true} if the specified path points to a directory
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
   * Tests if the specified path points to a directory in the given tree.
   *
   * @param repo a git repository
   * @param path a file path
   * @param treeId a tree id
   * @return {@code true} if the specified path points to a directory
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

}
