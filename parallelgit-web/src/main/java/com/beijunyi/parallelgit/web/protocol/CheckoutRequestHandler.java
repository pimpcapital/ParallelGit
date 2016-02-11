package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.web.workspace.Workspace;

public class CheckoutRequestHandler implements RequestHandler {

  @Override
  public String getType() {
    return "checkout";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Workspace workspace) throws IOException {
    GitFileSystem gfs;
    if(!workspace.isInitialized()) {
      gfs = Gfs.newFileSystem(request.getString("branch"), workspace.getRepo());
      workspace.setFileSystem(gfs);
    } else
      gfs = workspace.getFileSystem();
    return request.respond().ok(FileSystemStatus.readStatus(gfs));
  }

}
