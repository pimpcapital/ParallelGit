package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoHeadCommitException;
import com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GfsState.APPLYING_STASH;
import static com.beijunyi.parallelgit.utils.CommitUtils.getCommit;
import static org.eclipse.jgit.lib.Constants.STASH;
import static org.eclipse.jgit.merge.MergeStrategy.RECURSIVE;

public class GfsApplyStash extends GfsCommand<GfsCreateStash.Result> {

  private static final String LAST_STASH = STASH + "@{0}";

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
  @Override
  protected GfsState getCommandState() {
    return APPLYING_STASH;
  }

  @Nonnull
  @Override
  protected GfsCreateStash.Result doExecute(GfsStatusProvider.Update update) throws IOException {
    prepareStash();
    prepareMerger();
    return null;
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
    ResolveMerger ret = (ResolveMerger) strategy.newMerger(repo);
    ret.setCommitNames(new String[] { "stashed HEAD", "HEAD", "stash" });
    ret.setBase(stash);
    ret.setWorkingTreeIterator(new GfsTreeIterator(gfs));
    return ret;
  }

  private void mergeStash() throws IOException {
    if(merger.merge(head, stash)) {
    }

  }

}
