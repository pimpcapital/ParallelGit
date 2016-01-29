package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.PosixFileAttributeView;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;

public interface GitFileAttributeView extends PosixFileAttributeView {

  boolean isNew();

  boolean isModified();

  @Nullable
  AnyObjectId getObjectId() throws IOException;

  @Nonnull
  FileMode getFileMode();

}
