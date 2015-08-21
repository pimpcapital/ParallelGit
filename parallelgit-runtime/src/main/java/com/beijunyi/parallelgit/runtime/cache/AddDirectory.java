package com.beijunyi.parallelgit.runtime.cache;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.CacheHelper;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

public class AddDirectory extends CacheEditor {

  private DirectoryStream<Path> directoryStream;
  private Path sourcePath;
  private File sourceFile;

  public AddDirectory(@Nonnull String path) {
    super(path);
  }

  public void setDirectoryStream(@Nonnull DirectoryStream<Path> directoryStream) {
    this.directoryStream = directoryStream;
  }

  public void setSourcePath(@Nonnull Path sourcePath) {
    this.sourcePath = sourcePath;
  }

  public void setSourceFile(@Nonnull File sourceFile) {
    this.sourceFile = sourceFile;
  }

  private void addFile(@Nonnull byte[] bytes, boolean executable, @Nonnull String path, @Nonnull
  CacheStateProvider provider) throws IOException {
    ObjectId blobId = provider.getInserter().insert(Constants.OBJ_BLOB, bytes);
    FileMode mode = executable ? FileMode.EXECUTABLE_FILE : FileMode.REGULAR_FILE;
    CacheHelper.addFile(provider.getCurrentBuilder(), mode, path, blobId);
  }

  private void addFile(@Nonnull Path sourcePath, @Nonnull String path, @Nonnull
  CacheStateProvider provider) throws IOException {
    byte[] bytes;
    try(InputStream inputStream = Files.newInputStream(sourcePath)) {
     bytes = AddFile.toByteArray(inputStream).toByteArray();
    }
    addFile(bytes, Files.isExecutable(sourcePath), path, provider);
  }

  private void addFile(@Nonnull File sourceFile, @Nonnull String path, @Nonnull
  CacheStateProvider provider) throws IOException {
    byte[] bytes;
    try(InputStream inputStream = new FileInputStream(sourceFile)) {
      bytes = AddFile.toByteArray(inputStream).toByteArray();
    }
    addFile(bytes, sourceFile.canExecute(), path, provider);
  }

  @Nonnull
  private static String normalizeFilename(@Nonnull String filename) {
    int length = filename.length();
    while(length > 0) {
      char c = filename.charAt(length - 1);
      if(c == '/' || c == '\\')
        length--;
      else
        break;
    }
    if(length == 0)
      throw new IllegalArgumentException(filename + " is not a valid filename");
    if(length == filename.length())
      return filename;
    return filename.substring(0, length);
  }

  private void processDirectoryStream(@Nonnull DirectoryStream<Path> directoryStream, @Nonnull String base, @Nonnull
  CacheStateProvider provider) throws IOException {
    for(Path child : directoryStream) {
      String filename = normalizeFilename(child.getFileName().toString());
      String fullPath = base + filename;
      if(Files.isRegularFile(child))
        addFile(child, fullPath, provider);
      else if(Files.isDirectory(child))
        processPath(child, fullPath + "/", provider);
      else
        throw new UnsupportedOperationException(child.toString());
    }
  }

  private void processPath(@Nonnull Path sourcePath, @Nonnull String base, @Nonnull
  CacheStateProvider provider) throws IOException {
    try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(sourcePath)) {
      processDirectoryStream(directoryStream, base, provider);
    }
  }

  private void processFile(@Nonnull File sourceFile, @Nonnull String base, @Nonnull
  CacheStateProvider provider) throws IOException {
    File[] children = sourceFile.listFiles();
    if(children == null)
      throw new IllegalArgumentException(base + " is not a valid directory");
    for(File child : children) {
      String fullPath = base + child.getName();
      if(child.isFile())
        addFile(child, fullPath, provider);
      else if(child.isDirectory())
        processFile(child, fullPath + "/", provider);
      else
        throw new UnsupportedOperationException(child.toString());
    }
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    String base = path;
    if(!base.isEmpty() && !base.endsWith("/"))
      base += "/";
    if(directoryStream != null)
      processDirectoryStream(directoryStream, base, provider);
    else if(sourcePath != null)
      processPath(sourcePath, base, provider);
    else if(sourceFile != null)
      processFile(sourceFile, base, provider);
    else
      throw new IllegalStateException();
  }

}
