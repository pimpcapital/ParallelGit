package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public class ReadFileHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "read-file";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    Path path = gfs.getPath(request.getString("path"));
    String data = new String(Files.readAllBytes(path));
    return request.respond().ok(data);
  }
}
