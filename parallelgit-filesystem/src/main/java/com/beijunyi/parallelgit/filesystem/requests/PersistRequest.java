package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.AnyObjectId;

public final class PersistRequest extends GitFileSystemRequest<AnyObjectId> {

  public PersistRequest(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected AnyObjectId doExecute() throws IOException {
    return gfs.persist();
  }

}
