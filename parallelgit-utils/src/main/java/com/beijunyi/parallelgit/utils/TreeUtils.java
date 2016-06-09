package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

import static org.eclipse.jgit.lib.FileMode.*;

public final class TreeUtils {

  @Nonnull
  public static String normalizeNodePath(String path) {
    if(path.startsWith("/"))
      return path.substring(1);
    if(path.endsWith("/"))
      return path.substring(0, path.length() - 1);
    return path;
  }

  @Nonnull
  public static TreeWalk newTreeWalk(AnyObjectId tree, ObjectReader reader) throws IOException {
    TreeWalk tw = new TreeWalk(reader);
    tw.reset(tree);
    return tw;
  }

  @Nonnull
  public static TreeWalk newTreeWalk(AnyObjectId tree, Repository repo) throws IOException {
    return newTreeWalk(tree, repo.newObjectReader());
  }

  @Nullable
  public static TreeWalk forPath(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    return TreeWalk.forPath(reader, normalizeNodePath(path), tree);
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
  public static ObjectId getObjectId(TreeWalk tw) {
    return tw.getObjectId(0);
  }

  @Nullable
  public static ObjectId getObjectId(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk tw = forPath(path, tree, reader)) {
      return tw != null ? getObjectId(tw) : null;
    }
  }

  @Nullable
  public static ObjectId getObjectId(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getObjectId(path, tree, reader);
    }
  }

  @Nullable
  public static FileMode getFileMode(TreeWalk tw) {
    return tw.getFileMode(0);
  }

  @Nullable
  public static FileMode getFileMode(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk tw = forPath(path, tree, reader)) {
      return tw != null ? getFileMode(tw) : null;
    }
  }

  @Nullable
  public static FileMode getFileMode(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getFileMode(path, tree, reader);
    }
  }

  @Nonnull
  public static InputStream openFile(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    AnyObjectId blobId = getObjectId(path, tree, reader);
    if(blobId == null)
      throw new NoSuchFileException(path);
    return BlobUtils.openBlob(blobId, reader);
  }

  @Nonnull
  public static InputStream openFile(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return openFile(path, tree, reader);
    }
  }

  @Nonnull
  public static BlobSnapshot readFile(String path, ObjectId tree, ObjectReader reader) throws IOException {
    ObjectId blobId = getObjectId(path, tree, reader);
    if(blobId == null)
      throw new NoSuchFileException(path);
    return BlobUtils.readBlob(blobId, reader);
  }

  @Nonnull
  public static BlobSnapshot readFile(String path, ObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readFile(path, tree, reader);
    }
  }

  public static boolean isDirectory(TreeWalk tw) {
    return TREE.equals(getFileMode(tw));
  }

  public static boolean isDirectory(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk tw = forPath(path, tree, reader)) {
      return tw != null && isDirectory(tw);
    }
  }

  public static boolean isDirectory(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isDirectory(path, tree, reader);
    }
  }

  public static boolean isFile(TreeWalk tw) {
    return REGULAR_FILE.equals(getFileMode(tw)) || EXECUTABLE_FILE.equals(getFileMode(tw));
  }

  public static boolean isFile(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk tw = forPath(path, tree, reader)) {
      return tw != null && isFile(tw);
    }
  }

  public static boolean isFile(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isFile(path, tree, reader);
    }
  }

  public static boolean isSymbolicLink(TreeWalk tw) {
    return SYMLINK.equals(getFileMode(tw));
  }

  public static boolean isSymbolicLink(String path, AnyObjectId tree, ObjectReader reader) throws IOException {
    try(TreeWalk tw = forPath(path, tree, reader)) {
      return tw != null && isSymbolicLink(tw);
    }
  }

  public static boolean isSymbolicLink(String path, AnyObjectId tree, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isSymbolicLink(path, tree, reader);
    }
  }

  @Nonnull
  public static ObjectId insertTree(TreeFormatter tf, Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      ObjectId treeId = inserter.insert(tf);
      inserter.flush();
      return treeId;
    }
  }


}
