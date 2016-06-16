package com.beijunyi.parallelgit.filesystem.commands;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Result;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.merge;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Status.ALREADY_UP_TO_DATE;
import static com.beijunyi.parallelgit.utils.BranchUtils.createBranch;
import static java.nio.file.Files.*;
import static org.junit.Assert.*;

public class GfsMergeUpToDateTest extends AbstractParallelGitTest {

  private static final String OURS = "ours";
  private static final String THEIRS = "theirs";

  @Before
  public void setUp() throws Exception {
    initRepository();
    AnyObjectId base = commit();
    AnyObjectId theirs = commit(base);
    AnyObjectId ours = commit(theirs);
    createBranch(OURS, ours, repo);
    createBranch(THEIRS, theirs, repo);
  }

  @Test
  public void whenHeadIsAheadOfSourceBranch_theResultShouldBeAlreadyUpToDate() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      Result result = merge(gfs).source(THEIRS).execute();
      assertEquals(ALREADY_UP_TO_DATE, result.getStatus());
    }
  }

  @Test
  public void whenHeadIsAheadWithDirtyFile_theResultShouldBeAlreadyUpToDate() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      write(gfs.getPath("/some_file.txt"), someBytes());
      Result result = merge(gfs).source(THEIRS).execute();
      assertEquals(ALREADY_UP_TO_DATE, result.getStatus());
    }
  }

  @Test
  public void whenHeadIsAheadWithDirtyFile_theFileShouldStillExistInTheFileSystemAfterMerge() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      write(gfs.getPath("/test_file.txt"), someBytes());
      merge(gfs).source(THEIRS).execute();
      assertTrue(exists(gfs.getPath("/test_file.txt")));
    }
  }

}
