package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.commands.GfsCheckoutCommand;
import com.beijunyi.parallelgit.filesystem.exceptions.UnsuccessfulOperationException;
import com.beijunyi.parallelgit.web.protocol.model.Status;
import com.beijunyi.parallelgit.web.workspace.Workspace;

public class CheckoutRequestHandler extends AbstractGfsRequestHandler {

  @Override
  public String getType() {
    return "checkout";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    GfsCheckoutCommand.Result result = Gfs.checkout(gfs).setTarget(request.getString("branch")).execute();
    if(result.isSuccessful())
      return request.respond().ok(Status.of(gfs));
    throw new UnsuccessfulOperationException();
  }

}
