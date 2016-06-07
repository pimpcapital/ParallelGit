package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevTree;

public final class GitFileUtils {

  public static boolean exists(String file, AnyObjectId commit, ObjectReader reader) throws IOException {
    return TreeUtils.exists(file, getRootTree(commit, reader), reader);
  }

  public static boolean exists(String file, AnyObjectId commit, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return exists(file, commit, reader);
    }
  }

  public static boolean exists(String file, String commit, Repository repo) throws IOException {
    return exists(file, repo.resolve(commit), repo);
  }

  public static boolean isDirectory(String file, AnyObjectId commit, ObjectReader reader) throws IOException {
    return TreeUtils.isDirectory(file, getRootTree(commit, reader), reader);
  }

  public static boolean isDirectory(String file, AnyObjectId commit, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isDirectory(file, commit, reader);
    }
  }

  public static boolean isDirectory(String file, String commit, Repository repo) throws IOException {
    return isDirectory(file, repo.resolve(commit), repo);
  }

  public static boolean isFile(String file, AnyObjectId commit, ObjectReader reader) throws IOException {
    ObjectId root = getRootTree(commit, reader);
    return TreeUtils.isFile(file, root, reader);
  }

  public static boolean isFile(String file, AnyObjectId commit, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isFile(file, commit, reader);
    }
  }

  public static boolean isFile(String file, String commit, Repository repo) throws IOException {
    return isFile(file, repo.resolve(commit), repo);
  }

  public static boolean isSymbolicLink(String file, AnyObjectId commit, ObjectReader reader) throws IOException {
    return TreeUtils.isSymbolicLink(file, getRootTree(commit, reader), reader);
  }

  public static boolean isSymbolicLink(String file, AnyObjectId commit, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return isSymbolicLink(file, commit, reader);
    }
  }

  public static boolean isSymbolicLink(String file, String commit, Repository repo) throws IOException {
    return isSymbolicLink(file, repo.resolve(commit), repo);
  }

  @Nonnull
  public static InputStream openFile(String file, AnyObjectId commit, ObjectReader reader) throws IOException {
    AnyObjectId blobId = TreeUtils.getObjectId(file, getRootTree(commit, reader), reader);
    if(blobId == null)
      throw new NoSuchFileException(file);
    return BlobUtils.openBlob(blobId, reader);
  }

  @Nonnull
  public static InputStream openFile(String file, AnyObjectId commit, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return openFile(file, commit, reader);
    }
  }

  @Nonnull
  public static InputStream openFile(String file, String commit, Repository repo) throws IOException {
    return openFile(file, repo.resolve(commit), repo);
  }

  @Nonnull
  public static BlobSnapshot readFile(String file, AnyObjectId commit, ObjectReader reader) throws IOException {
    ObjectId blobId = TreeUtils.getObjectId(file, getRootTree(commit, reader), reader);
    if(blobId == null)
      throw new NoSuchFileException(file);
    return BlobUtils.readBlob(blobId, reader);
  }

  @Nonnull
  public static BlobSnapshot readFile(String file, AnyObjectId commit, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readFile(file, commit, reader);
    }
  }

  @Nonnull
  public static BlobSnapshot readFile(String file, String revision, Repository repo) throws IOException {
    return readFile(file, repo.resolve(revision), repo);
  }

  @Nonnull
  private static RevTree getRootTree(AnyObjectId commit, ObjectReader reader) throws IOException {
    return CommitUtils.getCommit(commit, reader).getTree();
  }

}
