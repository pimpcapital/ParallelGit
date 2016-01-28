package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;

public interface GitFileAttributeView {

  @Nullable
  AnyObjectId getObjectId() throws IOException;

}
