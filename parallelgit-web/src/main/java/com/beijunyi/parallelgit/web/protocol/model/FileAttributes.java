package com.beijunyi.parallelgit.web.protocol.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView;
import com.beijunyi.parallelgit.web.data.FileState;
import com.beijunyi.parallelgit.web.data.FileType;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

import static com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView.*;

public class FileAttributes {

  private final String name;
  private final String hash;
  private final FileType type;
  private final FileState state;

  public FileAttributes(@Nonnull String name, @Nonnull String hash, @Nonnull FileType type, @Nonnull FileState state) {
    this.name = name;
    this.hash = hash;
    this.type = type;
    this.state = state;
  }

  @Nonnull
  public static FileAttributes read(@Nonnull Path path) throws IOException {
    GfsFileAttributeView.Git view = Files.getFileAttributeView(path, GfsFileAttributeView.Git.class);
    Path namePath = path.getFileName();
    String name = namePath != null ? namePath.toString() : "";
    String hash = readHash(view);
    FileType type = readType(view);
    FileState state = readState(view);
    return new FileAttributes(name, hash, type, state);
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public String getHash() {
    return hash;
  }

  @Nonnull
  public FileType getType() {
    return type;
  }

  @Nonnull
  public FileState getState() {
    return state;
  }

  @Nonnull
  private static String readHash(@Nonnull GfsFileAttributeView.Git view) throws IOException {
    return view.getAttribute(OBJECT_ID, AnyObjectId.class).getName();
  }

  @Nonnull
  private static FileType readType(@Nonnull GfsFileAttributeView.Git view) throws IOException {
    FileMode mode = view.getAttribute(FILE_MODE, FileMode.class);
    if(mode.equals(FileMode.TREE))
      return FileType.DIRECTORY;
    if(mode.equals(FileMode.REGULAR_FILE) || mode.equals(FileMode.EXECUTABLE_FILE))
      return FileType.REGULAR_FILE;
    throw new UnsupportedOperationException();
  }

  @Nonnull
  private static FileState readState(@Nonnull GfsFileAttributeView.Git view) throws IOException {
    if(view.getBoolean(IS_MODIFIED))
      return view.getBoolean(IS_NEW) ? FileState.NEW : FileState.MODIFIED;
    return FileState.NORMAL;
  }



}
