package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public class RenameFileHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "rename-file";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    Path source = gfs.getPath(request.getString("path"));
    Path destination = source.resolveSibling(request.getString("filename"));
    Files.move(source, destination);
    return request.respond().ok();
  }
}
