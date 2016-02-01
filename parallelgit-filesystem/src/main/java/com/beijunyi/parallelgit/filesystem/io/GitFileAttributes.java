package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.PosixFileAttributes;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public interface GitFileAttributes extends PosixFileAttributes {

  boolean isNew() throws IOException;

  boolean isModified() throws IOException;

  @Nullable
  AnyObjectId getObjectId() throws IOException;

  @Nonnull
  FileMode getFileMode();

}
