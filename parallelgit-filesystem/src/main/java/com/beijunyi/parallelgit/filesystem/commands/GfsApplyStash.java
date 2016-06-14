package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator;
import com.beijunyi.parallelgit.filesystem.merge.MergeConflict;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.Merger;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GfsState.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsApplyStash.Result.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsApplyStash.Status.*;
import static com.beijunyi.parallelgit.filesystem.io.GfsDefaultCheckout.checkout;
import static com.beijunyi.parallelgit.filesystem.merge.MergeConflict.readConflicts;
import static com.beijunyi.parallelgit.utils.CommitUtils.getCommit;
import static java.util.Collections.unmodifiableMap;
import static org.eclipse.jgit.lib.Constants.STASH;
import static org.eclipse.jgit.merge.MergeStrategy.RECURSIVE;

public class GfsApplyStash extends GfsCommand<GfsApplyStash.Result> {

  private static final String LAST_STASH = makeStashId(0);

  private RevCommit head;
  private String stashId;
  private RevCommit stash;
  private MergeStrategy strategy = RECURSIVE;
  private ResolveMerger merger;

  public GfsApplyStash(GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  public GfsApplyStash stash(String stashId) throws IOException {
    this.stashId = stashId;
    return this;
  }

  @Nonnull
  public GfsApplyStash stash(int id) throws IOException {
    return stash(makeStashId(id));
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
    if(stashId == null) stashId = LAST_STASH;
    stash = getCommit(stashId, repo);
  }

  private void prepareMerger() throws IOException {
    merger = (ResolveMerger)strategy.newMerger(repo);
    merger.setBase(stash.getParent(0));
    merger.setWorkingTreeIterator(new GfsTreeIterator(gfs));
  }

  @Nonnull
  private GfsApplyStash.Result mergeStash(GfsStatusProvider.Update update) throws IOException {
    boolean success = merger.merge(head, stash);
    GfsApplyStash.Result ret;
    if(success) {
      updateFiles(merger);
      ret = success();
    } else {
      ret = conflicting(readConflicts(merger));
    }
    update.state(NORMAL);
    return ret;
  }

  private void updateFiles(Merger merger) throws IOException {
    AnyObjectId treeId = merger.getResultTreeId();
    checkout(gfs, treeId);
  }

  @Nonnull
  private static String makeStashId(int id) {
    return STASH + "@{" + id + "}";
  }

  public enum Status {
    SUCCESS,
    CONFLICTING
  }

  public static class Result implements GfsCommandResult {

    private final GfsApplyStash.Status status;
    private final Map<String, MergeConflict> conflicts;

    private Result(Status status, Map<String, MergeConflict> conflicts) {
      this.status = status;
      this.conflicts = unmodifiableMap(conflicts);
    }

    @Nonnull
    public static GfsApplyStash.Result success() {
      return new GfsApplyStash.Result(SUCCESS, Collections.<String, MergeConflict>emptyMap());
    }

    @Nonnull
    public static GfsApplyStash.Result conflicting(Map<String, MergeConflict> conflicts) {
      return new GfsApplyStash.Result(CONFLICTING, conflicts);
    }

    @Override
    public boolean isSuccessful() {
      return SUCCESS == status;
    }

    @Nonnull
    public Map<String, MergeConflict> getConflicts() {
      return conflicts;
    }
  }

}
