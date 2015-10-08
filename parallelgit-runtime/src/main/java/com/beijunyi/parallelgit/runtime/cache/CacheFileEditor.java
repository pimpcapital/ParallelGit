package com.beijunyi.parallelgit.runtime.cache;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.CacheUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;

public abstract class CacheFileEditor extends CacheEditor {

  public static int DEFAULT_BUFFER_SIZE = 1024 * 4;

  protected byte[] bytes;
  protected String content;
  protected InputStream inputStream;
  protected Path sourcePath;
  protected File sourceFile;
  protected FileMode mode;

  protected CacheFileEditor(@Nonnull String path) {
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
  static ByteArrayOutputStream toByteArray(@Nonnull InputStream inputStream) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[DEFAULT_BUFFER_SIZE];
    while ((nRead = inputStream.read(data, 0, data.length)) != -1)
      buffer.write(data, 0, nRead);
    buffer.flush();
    return buffer;
  }

  @Nonnull
  private static byte[] toByteArray(@Nonnull String content) {
    return Constants.encode(content);
  }

  @Nonnull
  private static byte[] readBytes(@Nonnull InputStream inputStream) throws IOException {
    return toByteArray(inputStream).toByteArray();
  }

  @Nonnull
  private static byte[] readBytes(@Nonnull Path sourcePath) throws IOException {
    return Files.readAllBytes(sourcePath);
  }

  @Nonnull
  private static byte[] readBytes(@Nonnull File sourceFile) throws IOException {
    try(InputStream inputStream = new FileInputStream(sourceFile)) {
      return readBytes(inputStream);
    }
  }


  protected void prepareFileMode() throws IOException {
    if(mode == null) {
      if(bytes != null || content != null || inputStream != null)
        mode = null;
      else if(sourcePath != null)
        mode = Files.isExecutable(sourcePath) ? FileMode.EXECUTABLE_FILE : FileMode.REGULAR_FILE;
      else if(sourceFile != null)
        mode = sourceFile.canExecute() ? FileMode.EXECUTABLE_FILE : FileMode.REGULAR_FILE;
      else
        throw new IllegalStateException();
    }
  }

  protected void prepareBytes() throws IOException {
    if(bytes == null) {
      if(content != null)
        bytes = toByteArray(content);
      else if(inputStream != null)
        bytes = readBytes(inputStream);
      else if(sourcePath != null)
        bytes = readBytes(sourcePath);
      else if(sourceFile != null)
        bytes = readBytes(sourceFile);
      else
        throw new IllegalStateException();
    }
  }

  protected void createEntry(@Nonnull CacheStateProvider provider) throws IOException {
    AnyObjectId blobId = provider.getInserter().insert(Constants.OBJ_BLOB, bytes);
    CacheUtils.addFile(path, mode != null ? mode : FileMode.REGULAR_FILE, blobId, provider.getCurrentBuilder());
  }

}
