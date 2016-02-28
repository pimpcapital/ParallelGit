package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.commands.GfsCheckoutCommand;
import com.beijunyi.parallelgit.filesystem.exceptions.UnsuccessfulOperationException;

public class CheckoutHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "checkout";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    GfsCheckoutCommand.Result result = Gfs.checkout(gfs).setTarget(request.getString("branch")).execute();
    if(result.isSuccessful())
      return request.respond().ok();
    throw new UnsuccessfulOperationException();
  }

}
