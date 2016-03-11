package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.web.protocol.model.Head;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

import static com.beijunyi.parallelgit.utils.BranchUtils.getBranches;
import static com.google.common.collect.Collections2.transform;
import static org.eclipse.jgit.lib.Constants.*;

public class ListBranchesHandler extends AbstractRepositoryRequestHandler {

  private static final BranchRefConverter BRANCH_REF_CONVERTER = new BranchRefConverter();
  private static final BranchSorter BRANCH_SORTER = new BranchSorter();

  @Nonnull
  @Override
  public String getType() {
    return "list-branches";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull Repository repo) throws IOException {
    List<String> branches = getSortedBranches(repo);
    List<Head> heads = Lists.transform(branches, new BranchHeadReader(repo));
    return request.respond().ok(heads);
  }


  @Nonnull
  private static List<String> getSortedBranches(@Nonnull Repository repo) throws IOException {
    List<String> ret = new ArrayList<>(transform(getBranches(repo).values(), BRANCH_REF_CONVERTER));
    Collections.sort(ret, BRANCH_SORTER);
    return ret;
  }

  private static class BranchSorter implements Comparator<String> {

    private static final String MASTER_REF = R_HEADS + MASTER;

    @Override
    public int compare(@Nonnull String b1, @Nonnull String b2) {
      if(MASTER_REF.equals(b1))
        return -1;
      if(MASTER_REF.equals(b2))
        return 1;
      return b1.compareTo(b2);
    }
  }

  private static class BranchRefConverter implements Function<Ref, String> {
    @Nonnull
    @Override
    public String apply(@Nullable Ref ref) {
      if(ref == null)
        throw new IllegalStateException();
      return ref.getName();
    }
  }

  private static class BranchHeadReader implements Function<String, Head> {

    private final Repository repo;

    public BranchHeadReader(@Nonnull Repository repo) {
      this.repo = repo;
    }

    @Nonnull
    @Override
    public Head apply(@Nullable String branch) {
      if(branch == null)
        throw new IllegalStateException();
      try {
        return Head.of(branch, repo);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

  }

}
