package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.*;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMergeCommand.Result;
import static com.beijunyi.parallelgit.utils.BranchUtils.createBranch;
import static org.eclipse.jgit.api.MergeResult.MergeStatus.ALREADY_UP_TO_DATE;

@Ignore
public class GfsMergeCommandTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initRepository();
  }

  @Test
  public void mergeBranchWhenCurrentBranchIsUpToDate_() throws Exception {
    AnyObjectId theirsHead = writeSomethingAndCommit(null);
    AnyObjectId oursHead = writeSomethingAndCommit(theirsHead);
    createBranch("ours", oursHead, repo);
    createBranch("theirs", theirsHead, repo);
    Result result;
    try(GitFileSystem gfs = newFileSystem("ours", repo)) {
      result = merge(gfs).source("theirs").execute();
    }
    Assert.assertEquals(ALREADY_UP_TO_DATE, result.getStatus());
  }

  @Nonnull
  private AnyObjectId writeSomethingAndCommit(@Nullable AnyObjectId parent) throws IOException {
    writeSomethingToCache();
    return commit(parent);
  }

}
