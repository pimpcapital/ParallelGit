package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.nio.file.Files;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.protocol.model.FileAttributes;
import com.beijunyi.parallelgit.web.workspace.Workspace;

public class GetFileAttributesHandler implements RequestHandler {

  @Override
  public String getType() {
    return "get-file-attributes";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Workspace workspace) throws IOException {
    GitFileSystem gfs = workspace.getFileSystem();
    FileAttributes attributes = FileAttributes.read(gfs.getPath(request.getString("path")));
    return request.respond().ok(attributes);
  }
}
