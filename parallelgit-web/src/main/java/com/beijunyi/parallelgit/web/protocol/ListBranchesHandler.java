package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.web.workspace.Workspace;
import org.eclipse.jgit.lib.Repository;

import static org.eclipse.jgit.lib.Constants.MASTER;

public class ListBranchesHandler implements RequestHandler {

  private static final BranchSorter BRANCH_SORTER = new BranchSorter();

  @Override
  public String getType() {
    return "list-branches";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Workspace workspace) throws IOException {
    Repository repo = workspace.getRepo();
    return request.respond().ok(getSortedBranches(repo));
  }


  @Nonnull
  private static List<String> getSortedBranches(@Nonnull Repository repo) throws IOException {
    List<String> ret = new ArrayList<>(BranchUtils.getBranches(repo).keySet());
    Collections.sort(ret, BRANCH_SORTER);
    return ret;
  }

  private static class BranchSorter implements Comparator<String> {
    @Override
    public int compare(@Nonnull String b1, @Nonnull String b2) {
      if(MASTER.equals(b1))
        return -1;
      if(MASTER.equals(b2))
        return 1;
      return b1.compareTo(b2);
    }
  }

}
