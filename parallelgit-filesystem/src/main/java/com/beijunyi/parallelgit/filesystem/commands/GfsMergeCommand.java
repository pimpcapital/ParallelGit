package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoChangeException;
import com.beijunyi.parallelgit.filesystem.io.GfsCheckout;
import com.beijunyi.parallelgit.filesystem.merge.GfsMergeNote;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RefUtils;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.CheckoutConflictException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.*;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GfsState.NORMAL;
import static com.beijunyi.parallelgit.filesystem.merge.GfsMergeNote.mergeSquash;
import static com.beijunyi.parallelgit.filesystem.utils.GfsPathUtils.toAbsolutePath;
import static com.beijunyi.parallelgit.utils.RefUtils.ensureBranchRefName;
import static java.util.Collections.singletonList;
import static org.eclipse.jgit.api.MergeResult.MergeStatus.*;

public final class GfsMergeCommand extends GfsCommand<GfsMergeCommand.Result> {

  private String branch;
  private Ref branchRef;
  private RevCommit headCommit;
  private String source;
  private Ref sourceRef;
  private RevCommit sourceHeadCommit;
  private boolean squash = false;
  private boolean commit = true;

  private PersonIdent committer;
  private String message;
  private MergeStrategy strategy = new StrategyRecursive();

  private DirCache cache;

  public GfsMergeCommand(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  @Override
  protected Result doExecute() throws IOException {
    prepareBranchHead();
    prepareTarget();
    prepareSourceCommit();
    prepareMessage();

    if(isUpToDate())
      return Result.upToDate(headCommit);

    if(canBeFastForwarded())
      return fastForward();

    return threeWayMerge();
  }

  @Nonnull
  public GfsMergeCommand source(@Nullable String branch) {
    this.source = branch;
    return this;
  }

  @Nonnull
  public GfsMergeCommand source(@Nullable Ref branchRef) {
    this.sourceRef = branchRef;
    return this;
  }

  @Nonnull
  public GfsMergeCommand squash(boolean squash) {
    this.squash = squash;
    return this;
  }

  @Nonnull
  public GfsMergeCommand commit(boolean commit) {
    this.commit = commit;
    return this;
  }

  @Nonnull
  public GfsMergeCommand committer(@Nullable PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public GfsMergeCommand message(@Nullable String message) {
    this.message = message;
    return this;
  }

  @Nonnull
  public GfsMergeCommand strategy(@Nullable MergeStrategy strategy) {
    this.strategy = strategy;
    return this;
  }

  private void prepareBranchHead() throws IOException {
    GfsStatusProvider status = gfs.getStatusProvider();

    if(!status.isAttached())
      throw new NoBranchException();
    branch = gfs.getStatusProvider().branch();

    branchRef = RefUtils.getBranchRef(branch, repo);
    if(branchRef == null)
      throw new NoSuchBranchException(ensureBranchRefName(branch));

    if(status.isInitialized())
      headCommit = status.commit();
  }

  private void prepareTarget() throws IOException {
    if(sourceRef == null) {
      sourceRef = RefUtils.getBranchRef(source, repo);
    }
  }

  private void prepareSourceCommit() throws IOException {
    sourceHeadCommit = CommitUtils.getCommit(sourceRef, repo);
  }

  private void prepareMessage() throws IOException {
    if(message == null) {
      if(squash) {
        List<RevCommit> squashedCommits = CommitUtils.findSquashableCommits(sourceHeadCommit, headCommit, repo);
        message = new SquashMessageFormatter().format(squashedCommits, branchRef);
      } else
        message = new MergeMessageFormatter().format(singletonList(sourceRef), branchRef);
    }
  }

  private boolean isUpToDate() throws IOException {
    return headCommit != null && CommitUtils.isMergedInto(sourceHeadCommit, headCommit, repo);
  }

  private boolean canBeFastForwarded() throws IOException {
    return headCommit == null || CommitUtils.isMergedInto(headCommit, sourceHeadCommit, repo);
  }

  @Nonnull
  private Result fastForward() throws IOException {
    GfsStatusProvider status = gfs.getStatusProvider();
    Result result;
    if(!tryCheckout(sourceHeadCommit))
      result = Result.checkoutConflict();
    else if(squash) {
      status.mergeNote(mergeSquash(message));
      result = Result.fastForwardSquashed();
    } else {
      BranchUtils.mergeBranch(branch, sourceHeadCommit, sourceRef, FAST_FORWARD.toString(), repo);
      result = Result.fastForward(sourceHeadCommit);
    }
    status.state(NORMAL);
    return result;
  }

  @Nonnull
  private Result threeWayMerge() throws IOException {
    Merger merger = prepareMerger();
    GfsStatusProvider status = gfs.getStatusProvider();
    AnyObjectId treeId;
    if(merger.merge(headCommit, sourceHeadCommit)) {
      treeId = merger.getResultTreeId();
      new GfsCheckout(gfs).checkout(treeId);
      if(commit && !squash) {
        prepareCommitter();
        RevCommit commit = CommitUtils.createCommit(message, treeId, committer, committer, Arrays.asList(headCommit, sourceHeadCommit), repo);
        BranchUtils.mergeBranch(branch, commit, sourceRef, "Merge made by " + strategy.getName() + ".", repo);
        status.state(NORMAL);
        return Result.merged(commit);
      }
    } else {
      if(merger instanceof ResolveMerger) {
        ResolveMerger rm = ResolveMerger.class.cast(merger);
        Map<String, MergeResult<? extends Sequence>> results = rm.getMergeResults();
        new GfsCheckout(gfs).ignoredFiles(getConflicts(rm)).checkout(cache);
        message = new MergeMessageFormatter().formatWithConflicts(message, rm.getUnmergedPaths());
        rm.getMergeResults();
      }
      return Result.conflicting();
    }
    if(squash) {
      status.mergeNote(GfsMergeNote.mergeSquash(message));
      status.state(NORMAL);
      return Result.mergedSquashed();
    }
    status.mergeNote(GfsMergeNote.mergeNoCommit(sourceHeadCommit, message));
    return Result.mergedNotCommitted();
  }

  private boolean tryCheckout(@Nonnull AnyObjectId tree) throws IOException {
    GfsCheckout checkout = new GfsCheckout(gfs);
    try {
      checkout.checkout(tree);
    } catch(CheckoutConflictException e) {
      return false;
    }
    return true;
  }

  @Nonnull
  private Merger prepareMerger() {
    Merger merger = strategy.newMerger(repo, true);
    if(merger instanceof ResolveMerger) {
      ResolveMerger rm = ((ResolveMerger)merger);
      rm.setCommitNames(new String[] {"BASE", "HEAD", sourceRef.getName()});
      cache = DirCache.newInCore();
      rm.setDirCache(cache);
    }
    return merger;
  }

  private void prepareCommitter() {
    if(committer == null)
      committer = new PersonIdent(repo);
  }

  @Nonnull
  private static Collection<String> getConflicts(@Nonnull ResolveMerger merger) {
    Collection<String> ret = new ArrayList<>(merger.getUnmergedPaths().size());
    for(String conflict : merger.getUnmergedPaths())
       ret.add(toAbsolutePath(conflict));
    return ret;
  }

  public static class Result implements GfsCommandResult {

    private final MergeStatus status;
    private final RevCommit commit;

    private Result(@Nonnull MergeStatus status, @Nullable RevCommit commit) {
      this.status = status;
      this.commit = commit;
    }

    @Nonnull
    public static Result checkoutConflict() {
      return new Result(CHECKOUT_CONFLICT, null);
    }

    @Nonnull
    public static Result upToDate(@Nonnull RevCommit commit) {
      return new Result(ALREADY_UP_TO_DATE, commit);
    }

    @Nonnull
    public static Result fastForward(@Nonnull RevCommit commit) {
      return new Result(FAST_FORWARD, commit);
    }

    @Nonnull
    public static Result fastForwardSquashed() {
      return new Result(FAST_FORWARD_SQUASHED, null);
    }

    @Nonnull
    public static Result merged(@Nonnull RevCommit commit) {
      return new Result(MERGED, commit);
    }

    @Nonnull
    public static Result mergedSquashed() {
      return new Result(MERGED_SQUASHED, null);
    }

    @Nonnull
    public static Result mergedNotCommitted() {
      return new Result(MERGED_NOT_COMMITTED, null);
    }

    @Nonnull
    public static Result conflicting() {
      return new Result(CONFLICTING, null);
    }

    @Override
    public boolean isSuccessful() {
      return commit != null;
    }

    @Nonnull
    public MergeStatus getStatus() {
      return status;
    }

    @Nonnull
    public RevCommit getCommit() {
      if(commit == null)
        throw new NoChangeException();
      return commit;
    }

  }

}
