package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public class WriteFileHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "write-file";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    Path path = gfs.getPath(request.getString("path"));
    Files.write(path, request.getString("data").getBytes());
    return request.respond().ok();
  }
}
