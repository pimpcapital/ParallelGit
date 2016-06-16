package com.beijunyi.parallelgit.filesystem.commands;

import java.nio.file.Files;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Result;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.merge;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Status.*;
import static com.beijunyi.parallelgit.utils.BranchUtils.createBranch;
import static java.nio.file.Files.write;
import static org.junit.Assert.*;

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
    createBranch(OURS, ours, repo);
    createBranch(THEIRS, theirs, repo);
  }

  @Test
  public void whenHeadIsBehindSourceBranch_theResultShouldBeFastForward() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      Result result = merge(gfs).source(THEIRS).execute();
      assertEquals(FAST_FORWARD, result.getStatus());
    }
  }

  @Test
  public void whenHeadIsBehindWithDirtyFile_theResultShouldBeFastForward() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      write(gfs.getPath("/some_file.txt"), someBytes());
      Result result = merge(gfs).source(THEIRS).execute();
      assertEquals(FAST_FORWARD, result.getStatus());
    }
  }

  @Test
  public void whenHeadIsBehindWithDirtyFile_theFileShouldStillExistInTheFileSystemAfterMerge() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      write(gfs.getPath("/test_file.txt"), someBytes());
      merge(gfs).source(THEIRS).execute();
      assertTrue(Files.exists(gfs.getPath("/test_file.txt")));
    }
  }

  @Test
  public void whenHeadIsBehindWithDirtyFileThatCauseConflict_theResultShouldBeCheckoutConflict() throws Exception {
    try(GitFileSystem gfs = Gfs.newFileSystem(OURS, repo)) {
      write(gfs.getPath("/their_file.txt"), someBytes());
      Result result = merge(gfs).source(THEIRS).execute();
      assertEquals(CHECKOUT_CONFLICT, result.getStatus());
    }
  }

}
