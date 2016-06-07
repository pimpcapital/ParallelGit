package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.*;

import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

public final class ObjectUtils {

  public static final ObjectId TRIVIAL_OBJECT = ObjectId.zeroId();

  public static boolean isTrivial(ObjectId id) {
    return TRIVIAL_OBJECT.equals(id);
  }

  @Nonnull
  public static ObjectId insertBlob(byte[] data, Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      ObjectId blobId = inserter.insert(OBJ_BLOB, data);
      inserter.flush();
      return blobId;
    }
  }

  @Nonnull
  public static ObjectId insertTree(TreeFormatter tf, Repository repo) throws IOException {
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      ObjectId blobId = inserter.insert(tf);
      inserter.flush();
      return blobId;
    }
  }

  @Nullable
  public static ObjectId findObject(String file, AnyObjectId commit, ObjectReader reader) throws IOException {
    return TreeUtils.getObjectId(file, CommitUtils.getCommit(commit, reader).getTree(), reader);
  }

  @Nullable
  public static ObjectId findObject(String file, AnyObjectId commit, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return findObject(file, commit, reader);
    }
  }

  public static long getBlobSize(AnyObjectId id, ObjectReader reader) throws IOException {
    return reader.getObjectSize(id, OBJ_BLOB);
  }

  public static long getBlobSize(AnyObjectId id, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getBlobSize(id, reader);
    }
  }

  @Nonnull
  public static InputStream openBlob(AnyObjectId id, ObjectReader reader) throws IOException {
    return reader.open(id).openStream();
  }

  @Nonnull
  public static InputStream openBlob(AnyObjectId id, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return openBlob(id, reader);
    }
  }

  @Nonnull
  public static BlobSnapshot readBlob(ObjectId id, ObjectReader reader) throws IOException {
    return BlobSnapshot.load(id, reader);
  }

  @Nonnull
  public static BlobSnapshot readBlob(ObjectId id, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readBlob(id, reader);
    }
  }

  @Nonnull
  public static TreeSnapshot readTree(ObjectId id, ObjectReader reader) throws IOException {
    return TreeSnapshot.load(id, reader);
  }

  @Nonnull
  public static TreeSnapshot readTree(ObjectId id, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return readTree(id, reader);
    }
  }

}
