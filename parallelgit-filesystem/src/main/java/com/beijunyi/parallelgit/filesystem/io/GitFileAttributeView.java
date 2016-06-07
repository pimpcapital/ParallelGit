package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.PosixFileAttributeView;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.FileMode;

public interface GitFileAttributeView extends PosixFileAttributeView {

  @Override
  String name();

  @Nonnull
  GitFileAttributes readAttributes() throws IOException;

  void setFileMode(FileMode mode);

}
