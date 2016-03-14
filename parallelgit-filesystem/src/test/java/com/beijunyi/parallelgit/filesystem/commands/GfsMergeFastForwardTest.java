package com.beijunyi.parallelgit.filesystem.commands;

import java.nio.file.Files;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.BranchUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.merge;
import static org.eclipse.jgit.api.MergeResult.MergeStatus.*;

public class GfsMergeFastForwardTest extends AbstractParallelGitTest {

  private static final String OURS = "ours";
  private static final String THEIRS = "theirs";

  @Before
  public void setUp() throws Exception {
    initRepository();
    AnyObjectId base = commit();
    AnyObjectId ours = commit(base);
    writeToCache("/their_file.txt");
    AnyObjectId theirs = commit(ours);
    BranchUtils.createBranch(OURS, ours, repo);
    BranchUtils.createBranch(THEIRS, theirs, repo);
  }

  @Test
  public void whenHeadIsBehindSourceBranch_theResultShouldBeFastForward() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      GfsMerge.Result result = merge(gfs).source(THEIRS).execute();
      Assert.assertEquals(FAST_FORWARD, result.getStatus());
    }
  }

  @Test
  public void whenHeadIsBehindWithDirtyFile_theResultShouldBeFastForward() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      Files.write(gfs.getPath("/some_file.txt"), someBytes());
      GfsMerge.Result result = merge(gfs).source(THEIRS).execute();
      Assert.assertEquals(FAST_FORWARD, result.getStatus());
    }
  }

  @Test
  public void whenHeadIsBehindWithDirtyFile_theFileShouldStillExistInTheFileSystemAfterMerge() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      Files.write(gfs.getPath("/test_file.txt"), someBytes());
      merge(gfs).source(THEIRS).execute();
      Assert.assertTrue(Files.exists(gfs.getPath("/test_file.txt")));
    }
  }

  @Test
  public void whenHeadIsBehindWithDirtyFileThatCauseConflict_theResultShouldBeCheckoutConflict() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      Files.write(gfs.getPath("/their_file.txt"), someBytes());
      GfsMerge.Result result = merge(gfs).source(THEIRS).execute();
      Assert.assertEquals(CHECKOUT_CONFLICT, result.getStatus());

    }
  }

}
