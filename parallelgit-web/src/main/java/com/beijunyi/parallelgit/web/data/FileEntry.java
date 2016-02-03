package com.beijunyi.parallelgit.web.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView;
import com.beijunyi.parallelgit.filesystem.io.GitFileAttributeView;
import org.eclipse.jgit.lib.FileMode;

import static com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView.*;

public class FileEntry implements Comparable<FileEntry> {

  private final String name;
  private final FileType type;
  private final FileState state;

  public FileEntry(@Nonnull String name, @Nonnull FileType type, @Nonnull FileState state) {
    this.name = name;
    this.type = type;
    this.state = state;
  }

  @Nonnull
  public static FileEntry read(@Nonnull Path path) throws IOException {
    GfsFileAttributeView.Git view = Files.getFileAttributeView(path, GfsFileAttributeView.Git.class);
    String name = path.getFileName().toString();
    FileType type = readType(view);
    FileState state = readState(view);
    return new FileEntry(name, type, state);
  }

  @Override
  public int compareTo(@Nonnull FileEntry that) {
    int typeCompare = getType().compareTo(that.getType());
    if(typeCompare != 0)
      return typeCompare;
    return getName().compareTo(that.getName());
  }

  @Nonnull
  public String getName() {
    return name;
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
