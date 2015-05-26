package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.ParallelGitException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public class TreeWalkHelper {

  /**
   * Resets the given {@link TreeWalk} to run over a set of existing trees.
   *
   * This method is the exception friendly version of {@link TreeWalk#reset(AnyObjectId...)} which has no checked
   * exception in the method signature. In the case that an {@link IOException} does occur, the source exception can be
   * retrieved from {@link ParallelGitException#getCause()}.
   *
   * @param treeWalk a tree walk
   */
  public static void reset(@Nonnull TreeWalk treeWalk, @Nonnull AnyObjectId... trees) {
    try {
      treeWalk.reset(trees);
    } catch(IOException e) {
      throw new ParallelGitException("Could not reset trees", e);
    }
  }

  /**
   * Enters into the current subtree of the given {@link TreeWalk}.
   *
   * This method is the exception friendly version of {@link TreeWalk#enterSubtree()} which has no checked exception in
   * the method signature. In the case that an {@link IOException} does occur, the source exception can be retrieved
   * from {@link ParallelGitException#getCause()}.
   *
   * @param treeWalk a tree walk
   */
  public static void enterSubtree(@Nonnull TreeWalk treeWalk) {
    try {
      treeWalk.enterSubtree();
    } catch(IOException e) {
      throw new ParallelGitException("Could not enter the subtree of " + treeWalk.getPathString(), e);
    }
  }

  /**
   * Advances the given {@link TreeWalk} to the next relevant entry.
   *
   * This method is the exception friendly version of {@link TreeWalk#next()} which has no checked exception in the
   * method signature. In the case that an {@link IOException} does occur, the source exception can be retrieved from
   * {@link ParallelGitException#getCause()}.
   *
   * @param treeWalk a tree walk
   */
  public static boolean next(@Nonnull TreeWalk treeWalk) {
    try {
      return treeWalk.next();
    } catch(IOException e) {
      throw new ParallelGitException("Could not advance the next entry after " + treeWalk.getPathString(), e);
    }
  }

  /**
   * Creates a new {@code TreeWalk} to run over the given tree.
   *
   * @param reader an object reader
   * @param treeId a tree id
   * @return a new tree walk to run over the given tree.
   */
  @Nonnull
  public static TreeWalk newTreeWalk(@Nonnull ObjectReader reader, @Nonnull AnyObjectId treeId) {
    TreeWalk treeWalk = new TreeWalk(reader);
    reset(treeWalk, treeId);
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
  public static TreeWalk newTreeWalk(@Nonnull Repository repo, @Nonnull AnyObjectId treeId) {
    return newTreeWalk(repo.newObjectReader(), treeId);
  }

  /**
   * Creates a new {@code TreeWalk} which filters to the specified path.
   *
   * This method is the exception friendly version of {@link TreeWalk#forPath(ObjectReader, String, AnyObjectId...)}
   * which has no checked exception in the method signature. In the case that an {@link IOException} does occur, the
   * source exception can be retrieved from {@link ParallelGitException#getCause()}.
   *
   * @param reader an object reader
   * @param path a path
   * @param treeId a tree id
   * @return a new tree walk which filters to the specified path
   */
  @Nullable
  public static TreeWalk forPath(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) {
    try {
      return TreeWalk.forPath(reader, path, treeId);
    } catch(IOException e) {
      throw new ParallelGitException("Could not create tree walk for " + path, e);
    }
  }

  @Nullable
  public static TreeWalk forPath(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) {
    return forPath(repo.newObjectReader(), path, treeId);
  }

  public static boolean exists(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) {
    return forPath(reader, path, treeId) != null;
  }

  /**
   * Tests if a node (either file or directory) exists at the specified path.
   *
   * @param repo a git repository
   * @param path a path
   * @param treeId a tree id
   * @return {@code true} if a node exists at the specified path
   */
  public static boolean exists(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) {
    return forPath(repo, path, treeId) != null;
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
  public static ObjectId getObjectId(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) {
    TreeWalk treeWalk = forPath(reader, path, treeId);
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
  public static ObjectId getObjectId(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) {
    TreeWalk treeWalk = forPath(repo, path, treeId);
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
  public static boolean isFile(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) {
    TreeWalk treeWalk = forPath(reader, path, treeId);
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
  public static boolean isFile(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) {
    TreeWalk treeWalk = forPath(repo, path, treeId);
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
  public static boolean isDirectory(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) {
    TreeWalk treeWalk = forPath(reader, path, treeId);
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
  public static boolean isDirectory(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) {
    TreeWalk treeWalk = forPath(repo, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isDirectory(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Adds the given tree into the provided {@link TreeWalk}.
   *
   * This method is the exception friendly version of {@link TreeWalk#addTree(AnyObjectId)} which has no checked
   * exception in the method signature. In the case that an {@link IOException} does occur, the source exception can be
   * retrieved from {@link ParallelGitException#getCause()}.
   *
   * @param treeWalk a tree walk
   * @param treeId a tree id
   */
  public static void addTree(@Nonnull TreeWalk treeWalk, @Nonnull AnyObjectId treeId) {
    try {
      treeWalk.addTree(treeId);
    } catch(IOException e) {
      throw new ParallelGitException("Could not add " + treeId.getName() + " to tree walk", e);
    }
  }

}
