package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.workspace.Workspace;

public class CreateNewFileHandler implements RequestHandler {

  @Override
  public String getType() {
    return "create-new-file";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Workspace workspace) throws IOException {
    GitFileSystem gfs = workspace.getFileSystem();
    Path path = gfs.getPath(request.getString("directory")).resolve(request.getString("filename"));
    OutputStream stream = Files.newOutputStream(path);
    stream.close();
    return request.respond().ok();
  }
}
