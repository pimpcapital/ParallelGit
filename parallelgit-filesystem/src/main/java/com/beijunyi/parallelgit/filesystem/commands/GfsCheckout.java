package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.io.GfsCheckoutConflict;
import com.beijunyi.parallelgit.filesystem.io.GfsDefaultCheckout;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RefUtils;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import static java.util.Collections.unmodifiableMap;

public class GfsCheckout extends GfsCommand<GfsCheckout.Result> {

  private String target;
  private boolean force = false;
  private boolean detach = false;

  private String targetBranch;
  private RevCommit targetCommit;


  public GfsCheckout(GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected Result doExecute(GfsStatusProvider.Update update) throws IOException {
    prepareFileSystem();
    prepareTarget();
    GfsDefaultCheckout checkout = new GfsDefaultCheckout(gfs, false);
    checkout.checkout(targetCommit.getTree());
    if(checkout.hasConflicts())
      return Result.checkoutConflicts(checkout.getConflicts());
    store.getRoot().updateOrigin(targetCommit.getTree());
    updateHead(update);
    return Result.success();
  }

  @Nonnull
  public GfsCheckout target(String target) {
    this.target = target;
    return this;
  }

  @Nonnull
  public GfsCheckout force(boolean force) {
    this.force = force;
    return this;
  }

  @Nonnull
  public GfsCheckout detach(boolean detach) {
    this.detach = detach;
    return this;
  }

  private void prepareFileSystem() {
    if(force)
      store.getRoot().reset();
  }

  private void prepareTarget() throws IOException {
    if(target == null)
      throw new NoBranchException();
    if(!detach) {
      if(BranchUtils.branchExists(target, repo)) {
        Ref branchRef = RefUtils.getBranchRef(target, repo);
        targetBranch = branchRef.getName();
        targetCommit = CommitUtils.getCommit(targetBranch, repo);
      }
    }
    if(targetCommit == null) {
      if(CommitUtils.exists(target, repo))
        targetCommit = CommitUtils.getCommit(target, repo);
      else
        throw new NoSuchBranchException(target);
    }
  }

  private void updateHead(GfsStatusProvider.Update update) {
    if(targetBranch == null)
      update.detach();
    else
      update.branch(targetBranch);
    update.commit(targetCommit);
  }

  public static class Result implements GfsCommandResult {

    private final boolean successful;
    private final Map<String, GfsCheckoutConflict> conflicts;

    private Result(boolean successful, Map<String, GfsCheckoutConflict> conflicts) {
      this.successful = successful;
      this.conflicts = unmodifiableMap(conflicts);
    }

    @Nonnull
    public static Result success() {
      return new Result(true, Collections.<String, GfsCheckoutConflict>emptyMap());
    }

    @Nonnull
    public static Result checkoutConflicts(Map<String, GfsCheckoutConflict> conflicts) {
      return new Result(false, conflicts);
    }

    @Override
    public boolean isSuccessful() {
      return successful;
    }

    public boolean hasConflicts() {
      return !conflicts.isEmpty();
    }

    @Nonnull
    public Map<String, GfsCheckoutConflict> getConflicts() {
      return conflicts;
    }
  }

}
