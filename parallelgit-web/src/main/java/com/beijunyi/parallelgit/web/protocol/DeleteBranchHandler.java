package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.Repository;

import static com.beijunyi.parallelgit.filesystem.Gfs.detach;
import static com.beijunyi.parallelgit.utils.BranchUtils.deleteBranch;

public class DeleteBranchHandler extends AbstractGfsRequestHandler {

  @Nonnull
  @Override
  public String getType() {
    return "delete-branch";
  }

  @Nonnull
  @Override
  protected ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    Repository repo = gfs.getRepository();
    GfsStatusProvider status = gfs.getStatusProvider();
    String name = request.getString("name");
    if(status.isAttached() && status.branch().equals(name))
      detach(gfs);
    deleteBranch(name, repo);
    return request.respond().ok();
  }
}
