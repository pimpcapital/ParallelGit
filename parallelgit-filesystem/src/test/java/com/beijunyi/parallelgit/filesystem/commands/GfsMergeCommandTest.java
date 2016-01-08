package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.*;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMergeCommand.Result;
import static com.beijunyi.parallelgit.utils.BranchUtils.createBranch;
import static org.eclipse.jgit.api.MergeResult.MergeStatus.*;

@Ignore
public class GfsMergeCommandTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initRepository();
  }

  @Test
  public void mergeWhenHeadIsAheadOfSourceBranch_resultShouldBeAlreadyUpToDate() throws Exception {
    AnyObjectId parentCommit = commit();
    prepareBranches(commit(parentCommit), parentCommit);
    Result result = mergeBranches();
    Assert.assertEquals(ALREADY_UP_TO_DATE, result.getStatus());
  }

  @Test
  public void mergeWhenHeadIsBehindSourceBranch_resultShouldBeFastForward() throws Exception {
    AnyObjectId parentCommit = commit();
    prepareBranches(parentCommit, commit(parentCommit));
    Result result = mergeBranches();
    Assert.assertEquals(FAST_FORWARD, result.getStatus());
  }

  private void prepareBranches(@Nonnull AnyObjectId ours, @Nonnull AnyObjectId theirs) throws IOException {
    createBranch("ours", ours, repo);
    createBranch("theirs", theirs, repo);
  }

  @Nonnull
  private Result mergeBranches() throws IOException {
    try(GitFileSystem gfs = newFileSystem("ours", repo)) {
      return merge(gfs).source("theirs").execute();
    }
  }

}
