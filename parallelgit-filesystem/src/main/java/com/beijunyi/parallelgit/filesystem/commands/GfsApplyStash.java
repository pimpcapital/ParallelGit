package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator;
import com.beijunyi.parallelgit.filesystem.merge.MergeConflict;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.merge.*;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GfsState.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsApplyStash.Result.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsApplyStash.Status.*;
import static com.beijunyi.parallelgit.filesystem.io.GfsDefaultCheckout.checkout;
import static com.beijunyi.parallelgit.filesystem.merge.GfsMergeCheckout.merge;
import static com.beijunyi.parallelgit.filesystem.merge.MergeConflict.readConflicts;
import static com.beijunyi.parallelgit.utils.CommitUtils.getCommit;
import static org.eclipse.jgit.lib.Constants.STASH;
import static org.eclipse.jgit.merge.MergeStrategy.RECURSIVE;

public class GfsApplyStash extends GfsCommand<GfsApplyStash.Result> {

  private static final String LAST_STASH = STASH + "@{0}";

  private RevCommit head;
  private String stashId;
  private RevCommit stash;
  private MergeStrategy strategy = RECURSIVE;
  private MergeFormatter formatter = new MergeFormatter();
  private ResolveMerger merger;
  private DirCache cache;

  public GfsApplyStash(GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  public GfsApplyStash stash(String stashId) throws IOException {
    this.stashId = stashId;
    return this;
  }

  @Nonnull
  @Override
  protected GfsState getCommandState() {
    return APPLYING_STASH;
  }

  @Nonnull
  @Override
  protected GfsApplyStash.Result doExecute(GfsStatusProvider.Update update) throws IOException {
    prepareHead();
    prepareStash();
    prepareMerger();
    return mergeStash(update);
  }

  private void prepareHead() throws IOException {
    if(!status.isInitialized())
      throw new NoHeadCommitException();
    head = status.commit();
  }

  private void prepareStash() throws IOException {
    if(stashId == null)
      stashId = LAST_STASH;
    stash = getCommit(stashId, repo);
  }

  @Nonnull
  private ResolveMerger prepareMerger() throws IOException {
    ResolveMerger ret = (ResolveMerger)strategy.newMerger(repo);
    cache = DirCache.newInCore();
    ret.setDirCache(cache);
    ret.setCommitNames(new String[]{"stashed HEAD", "HEAD", "stash"});
    ret.setBase(stash);
    ret.setWorkingTreeIterator(new GfsTreeIterator(gfs));
    return ret;
  }

  @Nonnull
  private GfsApplyStash.Result mergeStash(GfsStatusProvider.Update update) throws IOException {
    boolean success = merger.merge(head, stash);
    GfsApplyStash.Result ret;
    if(success) {
      updateFileSystemStatus(merger);
      ret = success();
    } else {
      writeConflicts(merger);
      ret = conflicting();
    }
    update.state(NORMAL);
    return ret;
  }

  private void updateFileSystemStatus(Merger merger) throws IOException {
    AnyObjectId treeId = merger.getResultTreeId();
    checkout(gfs, treeId);
  }

  private void writeConflicts(ResolveMerger merger) throws IOException {
    Map<String, MergeConflict> conflicts = readConflicts(merger);
    merge(gfs)
      .handleConflicts(conflicts)
      .withFormatter(formatter)
      .checkout(cache);
  }

  public enum Status {
    SUCCESS,
    CONFLICTING
  }

  public static class Result implements GfsCommandResult {

    private final GfsApplyStash.Status status;

    private Result(GfsApplyStash.Status status) {
      this.status = status;
    }

    @Nonnull
    public static GfsApplyStash.Result success() {
      return new GfsApplyStash.Result(SUCCESS);
    }

    @Nonnull
    public static GfsApplyStash.Result conflicting() {
      return new GfsApplyStash.Result(CONFLICTING);
    }

    @Override
    public boolean isSuccessful() {
      return SUCCESS == status;
    }
  }

}
