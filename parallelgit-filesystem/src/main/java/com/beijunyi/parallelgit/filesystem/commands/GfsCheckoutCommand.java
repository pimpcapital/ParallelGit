package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.io.GfsCheckout;
import com.beijunyi.parallelgit.filesystem.io.GfsCheckoutConflict;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RefUtils;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GfsState.NORMAL;

public final class GfsCheckoutCommand extends GfsCommand<GfsCheckoutCommand.Result> {

  private String target;
  private boolean force = false;
  private boolean detach = false;

  private String targetBranch;
  private RevCommit targetCommit;


  public GfsCheckoutCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException {
    prepareFileSystem();
    prepareTarget();
    GfsCheckout checkout = new GfsCheckout(gfs, false);
    checkout.checkout(targetCommit.getTree());
    update.state(NORMAL);
    if(checkout.hasConflicts())
      return Result.checkoutConflicts(checkout.getConflicts());
    store.getRoot().updateOrigin(targetCommit.getTree());
    updateHead(update);
    return Result.success();
  }

  @Nonnull
  @Override
  protected GfsState getCommandState() {
    return GfsState.CHECKING_OUT;
  }

  @Nonnull
  public GfsCheckoutCommand setTarget(@Nonnull String target) {
    this.target = target;
    return this;
  }

  @Nonnull
  public GfsCheckoutCommand force(boolean force) {
    this.force = force;
    return this;
  }

  @Nonnull
  public GfsCheckoutCommand detach(boolean force) {
    this.force = force;
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
      Ref branchRef = RefUtils.getBranchRef(target, repo);
      if(branchRef != null) {
        targetBranch = branchRef.getName();
        targetCommit = CommitUtils.getCommit(targetBranch, repo);
      }
    }
    if(targetCommit == null) {
      if(CommitUtils.commitExists(target, repo))
        targetCommit = CommitUtils.getCommit(target, repo);
      else
        throw new NoSuchBranchException(target);
    }
  }

  private void updateHead(@Nonnull GfsStatusProvider.Update update) {
    if(targetBranch == null)
      update.detach();
    else
      update.branch(targetBranch);
    update.commit(targetCommit);
  }

  public static class Result implements GfsCommandResult {

    private final boolean successful;
    private final Map<String, GfsCheckoutConflict> conflicts;

    private Result(boolean successful, @Nullable Map<String, GfsCheckoutConflict> conflicts) {
      this.successful = successful;
      this.conflicts = conflicts;
    }

    @Nonnull
    public static Result success() {
      return new Result(true, null);
    }

    @Nonnull
    public static Result checkoutConflicts(@Nonnull Map<String, GfsCheckoutConflict> conflicts) {
      return new Result(false, conflicts);
    }

    @Override
    public boolean isSuccessful() {
      return successful;
    }

    @Nonnull
    public Map<String, GfsCheckoutConflict> getConflicts() {
      if(conflicts == null)
        throw new IllegalStateException();
      return conflicts;
    }
  }

}
