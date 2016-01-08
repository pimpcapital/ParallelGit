package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.nio.file.Files;
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
  public void mergeWhenHeadIsAheadWithUnstashedFile_resultShouldBeAlreadyUpToDate() throws Exception {
    AnyObjectId parentCommit = commit();
    prepareBranches(commit(parentCommit), parentCommit);
    Result result;
    try(GitFileSystem gfs = prepareFileSystem()) {
      Files.write(gfs.getPath("/some_file.txt"), someBytes());
      result = merge(gfs).source("theirs").execute();
    }
    Assert.assertEquals(ALREADY_UP_TO_DATE, result.getStatus());
  }

  @Test
  public void mergeWhenHeadIsAheadWithUnstashedFile_theUnstashedFileShouldExistInTheFileSystemAfterMerge() throws Exception {
    AnyObjectId parentCommit = commit();
    prepareBranches(commit(parentCommit), parentCommit);
    try(GitFileSystem gfs = prepareFileSystem()) {
      Files.write(gfs.getPath("/some_file.txt"), someBytes());
      merge(gfs).source("theirs").execute();
      Assert.assertTrue(Files.exists(gfs.getPath("/some_file.txt")));
    }
  }

  @Test
  public void mergeWhenHeadIsBehindSourceBranch_resultShouldBeFastForward() throws Exception {
    AnyObjectId parentCommit = commit();
    prepareBranches(parentCommit, commit(parentCommit));
    Result result = mergeBranches();
    Assert.assertEquals(FAST_FORWARD, result.getStatus());
  }

  @Test
  public void mergeWhenHeadIsBehindWithUnstashedFile_resultShouldBeFastForward() throws Exception {
    AnyObjectId parentCommit = commit();
    prepareBranches(parentCommit, commit(parentCommit));
    Result result;
    try(GitFileSystem gfs = prepareFileSystem()) {
      Files.write(gfs.getPath("/some_file.txt"), someBytes());
      result = merge(gfs).source("theirs").execute();
    }
    Assert.assertEquals(FAST_FORWARD, result.getStatus());
  }

  private void prepareBranches(@Nonnull AnyObjectId ours, @Nonnull AnyObjectId theirs) throws IOException {
    createBranch("ours", ours, repo);
    createBranch("theirs", theirs, repo);
  }

  @Nonnull
  private GitFileSystem prepareFileSystem() throws IOException {
    return newFileSystem("ours", repo);
  }

  @Nonnull
  private Result mergeBranches() throws IOException {
    try(GitFileSystem gfs = prepareFileSystem()) {
      return merge(gfs).source("theirs").execute();
    }
  }

}
