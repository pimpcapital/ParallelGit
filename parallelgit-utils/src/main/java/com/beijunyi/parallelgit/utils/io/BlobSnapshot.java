package com.beijunyi.parallelgit.utils.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;

import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

public class BlobSnapshot extends ObjectSnapshot<byte[]> {

  private final ObjectReader reader;

  private BlobSnapshot(ObjectReader reader, @Nullable ObjectId id) {
    super(null, id);
    this.reader = reader;
  }

  private BlobSnapshot(ObjectReader reader) {
    this(null, null);
  }


  private BlobSnapshot(byte[] data) {
    super(data, null);
    reader = null;
  }

  @Nonnull
  @Override
  public byte[] getData() throws IOException {
    if (data == null) {
      loadData();
    }
    return data;
  }

  private void loadData() throws IOException {
    synchronized (reader) {
      ObjectLoader loader = reader.open(id);
      if (loader.getSize() > Integer.MAX_VALUE - 8) {
        throw new IOException("Can't load object" + id + ": size is greater than can fit in a Java array");
      }
      int len = (int) loader.getSize();
      data = new byte[len];
      int offset = 0;
      while (offset < len) {
        int bytesRead = loader.openStream().read(data, offset, len - offset);
        if (bytesRead <= 0) {
          throw new IOException("Unexpected EOF reading " + id);
        }
        offset += bytesRead;
      }
    }
  }

  @Nonnull
  public static BlobSnapshot load(ObjectId id, ObjectReader reader) throws IOException {
    return new BlobSnapshot(reader, id);
  }

  @Nonnull
  public static BlobSnapshot load(ObjectId id, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return load(id, reader);
    }
  }

  @Nonnull
  public static BlobSnapshot capture(byte[] bytes) {
    return new BlobSnapshot(bytes);
  }

  @Nonnull
  @Override
  public ObjectId save(ObjectInserter inserter) throws IOException {
    return inserter.insert(OBJ_BLOB, getData());
  }

  @Override
  protected int getType() {
    return OBJ_BLOB;
  }

  @Nonnull
  @Override
  protected byte[] toByteArray(byte[] bytes) {
    return bytes;
  }

  public InputStream getInputStream() throws IOException {
    synchronized (reader) {
      return reader.open(id).openStream();
    }
  }
}
