package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.protocol.model.FileAttributes;

public class CreateDirectoryHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "create-directory";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    Path path = gfs.getPath(request.getString("directory")).resolve(request.getString("name"));
    Files.createDirectory(path);
    FileAttributes attributes = FileAttributes.read(path);
    return request.respond().ok(attributes);
  }
}
