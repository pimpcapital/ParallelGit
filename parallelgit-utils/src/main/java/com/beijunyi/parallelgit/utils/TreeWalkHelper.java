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
  public static AnyObjectId getObject(@Nonnull TreeWalk treeWalk) {
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
  public static AnyObjectId getObject(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
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
  public static AnyObjectId getObject(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
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
   * Tests if a tree walk entry is attached to a blob object.
   *
   * @param   treeWalk
   *          the tree walk entry to test
   * @return  {@code true} if the given tree walk entry is attached to a blob object
   */
  public static boolean isBlob(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB;
  }

  /**
   * Tests if a file exists at the given path.
   *
   * @param   reader
   *          an object reader
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if a file exists at the given path
   */
  public static boolean isFileOrSymbolicLink(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isBlob(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a file exists at the given path.
   *
   * @param   repo
   *          a git repository
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if a file exists at the given path
   */
  public static boolean isFileOrSymbolicLink(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isBlob(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a tree walk entry is attached to a tree object.
   *
   * @param   treeWalk
   *          the tree walk entry to test
   * @return  {@code true} if the given tree walk entry is attached to a tree object
   */
  public static boolean isTree(@Nonnull TreeWalk treeWalk) {
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
      return isTree(treeWalk);
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
      return isTree(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a tree walk entry is attached to a regular file blob.
   *
   * @param   treeWalk
   *          the tree walk entry to test
   * @return  {@code true} if the given tree walk entry is attached to a regular file blob
   */
  public static boolean isRegular(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.REGULAR_FILE;
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
  public static boolean isRegularFile(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isRegular(treeWalk);
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
  public static boolean isRegularFile(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isRegular(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a tree walk entry is attached to an executable file blob.
   *
   * @param   treeWalk
   *          the tree walk entry to test
   * @return  {@code true} if the given tree walk entry is attached to an executable file blob
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
  public static boolean isExecutableFile(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
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
  public static boolean isExecutableFile(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isExecutable(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a tree walk entry is attached to either a regular or an executable file blob.
   *
   * @param   treeWalk
   *          the tree walk entry to test
   * @return  {@code true} if the given tree walk entry is attached to either a regular or an executable file blob
   */
  public static boolean isRegularOrExecutable(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.REGULAR_FILE || treeWalk.getFileMode(0) == FileMode.EXECUTABLE_FILE;
  }

  /**
   * Tests if a file is either regular or executable.
   *
   * @param   reader
   *          an object reader
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if the specified file is either regular or executable
   */
  public static boolean isRegularOrExecutableFile(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isRegularOrExecutable(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a file is either regular or executable.
   *
   * @param   repo
   *          a git repository
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if the specified file is either regular or executable
   */
  public static boolean isRegularOrExecutableFile(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isRegularOrExecutable(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a tree walk entry is attached to an symbolic link blob.
   *
   * @param   treeWalk
   *          the tree walk entry to test
   * @return  {@code true} if the given tree walk entry is attached to an symbolic link blob
   */
  public static boolean isSymbolicLink(@Nonnull TreeWalk treeWalk) {
    return treeWalk.getFileMode(0) == FileMode.SYMLINK;
  }

  /**
   * Tests if a file is a symbolic link.
   *
   * @param   reader
   *          an object reader
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if the specified file is a symbolic link
   */
  public static boolean isSymbolicLink(@Nonnull ObjectReader reader, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(reader, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isSymbolicLink(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

  /**
   * Tests if a file is a symbolic link blob.
   *
   * @param   repo
   *          a git repository
   * @param   path
   *          the path to the file to test
   * @param   treeId
   *          the base tree
   * @return  {@code true} if the specified file is a symbolic link
   */
  public static boolean isSymbolicLink(@Nonnull Repository repo, @Nonnull String path, @Nonnull AnyObjectId treeId) throws IOException {
    TreeWalk treeWalk = TreeWalk.forPath(repo, path, treeId);
    if(treeWalk == null)
      return false;
    try {
      return isSymbolicLink(treeWalk);
    } finally {
      treeWalk.release();
    }
  }

}
