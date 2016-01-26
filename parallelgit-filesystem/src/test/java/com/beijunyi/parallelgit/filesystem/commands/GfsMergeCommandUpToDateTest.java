package com.beijunyi.parallelgit.filesystem.commands;

import java.nio.file.Files;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.BranchUtils;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.merge;
import static org.eclipse.jgit.api.MergeResult.MergeStatus.ALREADY_UP_TO_DATE;

public class GfsMergeCommandUpToDateTest extends AbstractParallelGitTest {

  private static final String OURS = "ours";
  private static final String THERIS = "theirs";

  @Before
  public void setUp() throws Exception {
    initRepository();
    AnyObjectId base = commit();
    AnyObjectId theirs = commit(base);
    AnyObjectId ours = commit(theirs);
    BranchUtils.createBranch(OURS, ours, repo);
    BranchUtils.createBranch(THERIS, theirs, repo);
  }

  @Test
  public void whenHeadIsAheadOfSourceBranch_theResultShouldBeAlreadyUpToDate() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      GfsMergeCommand.Result result = merge(gfs).source("theirs").execute();
      Assert.assertEquals(ALREADY_UP_TO_DATE, result.getStatus());
    }
  }

  @Test
  public void whenHeadIsAheadWithDirtyFile_theResultShouldBeAlreadyUpToDate() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      Files.write(gfs.getPath("/some_file.txt"), someBytes());
      GfsMergeCommand.Result result = merge(gfs).source("theirs").execute();
      Assert.assertEquals(ALREADY_UP_TO_DATE, result.getStatus());
    }
  }

  @Test
  public void whenHeadIsAheadWithDirtyFile_theFileShouldStillExistInTheFileSystemAfterMerge() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      Files.write(gfs.getPath("/test_file.txt"), someBytes());
      merge(gfs).source("theirs").execute();
      Assert.assertTrue(Files.exists(gfs.getPath("/test_file.txt")));
    }
  }

}
