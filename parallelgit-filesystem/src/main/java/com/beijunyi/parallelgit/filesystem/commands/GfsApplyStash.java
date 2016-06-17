package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import com.beijunyi.parallelgit.filesystem.merge.MergeConflict;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.merge.*;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.commands.GfsApplyStash.Result.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsApplyStash.Status.*;
import static com.beijunyi.parallelgit.filesystem.io.GfsDefaultCheckout.checkout;
import static com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator.iterateRoot;
import static com.beijunyi.parallelgit.filesystem.merge.GfsMergeCheckout.handleConflicts;
import static com.beijunyi.parallelgit.filesystem.merge.MergeConflict.readConflicts;
import static com.beijunyi.parallelgit.utils.CommitUtils.getCommit;
import static java.util.Collections.unmodifiableMap;
import static org.eclipse.jgit.dircache.DirCache.newInCore;
import static org.eclipse.jgit.lib.Constants.STASH;
import static org.eclipse.jgit.merge.MergeStrategy.RECURSIVE;

public class GfsApplyStash extends GfsCommand<GfsApplyStash.Result> {

  private static final String LAST_STASH = makeStashId(0);

  private MergeStrategy strategy = RECURSIVE;
  private MergeFormatter formatter = new MergeFormatter();
  private DirCache cache = newInCore();

  private RevCommit head;
  private String stashId;
  private RevCommit stash;
  private ResolveMerger merger;

  public GfsApplyStash(GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  public GfsApplyStash stash(String id) throws IOException {
    this.stashId = id;
    return this;
  }

  @Nonnull
  public GfsApplyStash stash(int id) throws IOException {
    return stash(makeStashId(id));
  }

  @Nonnull
  @Override
  protected GfsApplyStash.Result doExecute(GfsStatusProvider.Update update) throws IOException {
    prepareHead();
    prepareStash();
    prepareMerger();
    return mergeStash();
  }

  private void prepareHead() throws IOException {
    if(!status.isInitialized()) throw new NoHeadCommitException();
    head = status.commit();
  }

  private void prepareStash() throws IOException {
    if(stashId == null) stashId = LAST_STASH;
    stash = getCommit(stashId, repo);
  }

  private void prepareMerger() throws IOException {
    merger = (ResolveMerger)strategy.newMerger(repo, true);
    merger.setBase(stash.getParent(0));
    merger.setCommitNames(new String[] {"BASE", "Updated upstream", "Stashed changes"});
    merger.setWorkingTreeIterator(iterateRoot(gfs));
  }

  @Nonnull
  private GfsApplyStash.Result mergeStash() throws IOException {
    boolean success = merger.merge(head, stash);
    GfsApplyStash.Result ret;
    if(success) {
      checkoutFiles(merger);
      ret = success();
    } else {
      writeConflicts(merger);
      ret = conflicting(readConflicts(merger));
    }
    return ret;
  }

  private void checkoutFiles(Merger merger) throws IOException {
    AnyObjectId treeId = merger.getResultTreeId();
    checkout(gfs, treeId);
  }

  private void writeConflicts(ResolveMerger merger) throws IOException {
    Map<String, MergeConflict> conflicts = readConflicts(merger);
    handleConflicts(gfs, conflicts)
      .withFormatter(formatter)
      .checkout(cache);
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

    public boolean hasConflicts() {
      return CONFLICTING == status;
    }

    @Nonnull
    public Map<String, MergeConflict> getConflicts() {
      return conflicts;
    }
  }

}
