package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsState;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoCommitException;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GfsState.APPLYING_STASH;
import static com.beijunyi.parallelgit.utils.CommitUtils.getCommit;

public class GfsApplyStash extends GfsCommand<GfsCreateStash.Result> {

  private String stashId;
  private RevCommit stash;

  public GfsApplyStash(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  public GfsApplyStash stash(@Nonnull String stashId) throws IOException {
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
  protected GfsCreateStash.Result doExecute(@Nonnull GfsStatusProvider.Update update) throws IOException {
    prepareStash();
    return null;
  }

  private void prepareStash() throws IOException {
    if(stashId == null)
      throw new NoCommitException();
    stash = getCommit(stashId, repo);
  }

}
