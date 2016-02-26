package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public class DiffFilesRequestHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "diff-files";
  }

  @Nonnull
  @Override
  protected ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    
    return null;
  }
}
