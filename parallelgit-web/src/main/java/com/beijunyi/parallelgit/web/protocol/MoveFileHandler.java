package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.protocol.model.FileAttributes;

public class MoveFileHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "move-file";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    Path source = gfs.getPath(request.getString("source"));
    Path destination = gfs.getPath(request.getString("directory")).resolve(request.getString("name"));
    Files.move(source, destination);
    FileAttributes attributes = FileAttributes.read(destination);
    return request.respond().ok(attributes);
  }
}
