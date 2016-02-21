package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.protocol.model.FileAttributes;
import com.beijunyi.parallelgit.web.workspace.Workspace;

public class CreateFileHandler extends AbstractGfsRequestHandler {

  @Override
  public String getType() {
    return "create-file";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    Path path = gfs.getPath(request.getString("directory")).resolve(request.getString("name"));
    Files.write(path, new byte[0]);
    FileAttributes attributes = FileAttributes.read(path);
    return request.respond().ok(attributes);
  }
}
