package com.beijunyi.parallelgit.util.builder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.util.DirCacheHelper;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

class AddFile extends CacheEditor {

  public static int DEFAULT_BUFFER_SIZE = 1024 * 4;

  private byte[] bytes;
  private String content;
  private InputStream inputStream;
  private Path sourcePath;
  private File sourceFile;
  private FileMode mode;

  AddFile(@Nonnull String path) {
    super(path);
  }

  public void setBytes(@Nonnull byte[] bytes) {
    this.bytes = bytes;
  }

  public void setContent(@Nonnull String content) {
    this.content = content;
  }

  public void setInputStream(@Nonnull InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public void setSourcePath(@Nonnull Path sourcePath) {
    this.sourcePath = sourcePath;
  }

  public void setSourceFile(@Nonnull File sourceFile) {
    this.sourceFile = sourceFile;
  }

  public void setMode(@Nonnull FileMode mode) {
    this.mode = mode;
  }

  @Nonnull
  private static byte[] toByteArray(@Nonnull String content) {
    return Constants.encode(content);
  }

  @Nonnull
  private static byte[] toByteArray(@Nonnull InputStream inputStream) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[DEFAULT_BUFFER_SIZE];
    while ((nRead = inputStream.read(data, 0, data.length)) != -1)
      buffer.write(data, 0, nRead);
    buffer.flush();
    return buffer.toByteArray();
  }

  @Nonnull
  private static byte[] readBytes(@Nonnull Path sourcePath) throws IOException {
    return Files.readAllBytes(sourcePath);
  }

  @Nonnull
  private static byte[] readBytes(@Nonnull File sourceFile) throws IOException {
    try(InputStream inputStream = new FileInputStream(sourceFile)) {
      return toByteArray(inputStream);
    }
  }

  @Override
  protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
    if(bytes == null) {
      if(content != null)
        bytes = toByteArray(content);
      else if(inputStream != null)
        bytes = toByteArray(inputStream);
      else if(sourcePath != null)
        bytes = readBytes(sourcePath);
      else if(sourceFile != null)
        bytes = readBytes(sourceFile);
      else
        throw new IllegalStateException();
    }
    ObjectId blobId = provider.getInserter().insert(Constants.OBJ_BLOB, bytes);
    DirCacheHelper.addFile(provider.getCurrentBuilder(), mode != null ? mode : FileMode.REGULAR_FILE, path, blobId);
  }

}
