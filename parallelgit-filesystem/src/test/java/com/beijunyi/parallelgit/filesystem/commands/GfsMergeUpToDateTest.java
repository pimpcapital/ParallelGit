package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.ParallelGitMergeTest;
import com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Result;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.merge;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Status.ALREADY_UP_TO_DATE;
import static com.beijunyi.parallelgit.utils.BranchUtils.getHeadCommit;
import static java.nio.file.Files.*;
import static org.junit.Assert.*;

public class GfsMergeUpToDateTest extends AbstractParallelGitTest implements ParallelGitMergeTest {

  private GitFileSystem gfs;

  @Before
  public void setUp() throws Exception {
    initRepository();
    AnyObjectId base = commit();
    AnyObjectId theirs = commitToBranch(THEIRS, base);
    commitToBranch(OURS, theirs);
    gfs = Gfs.newFileSystem(OURS, repo);
  }

  @After
  public void tearDown() throws IOException {
    if(gfs != null) {
      gfs.close();
      gfs = null;
    }
  }

  @Test
  public void mergeWhenHeadIsAheadOfSourceBranch_theResultShouldBeAlreadyUpToDate() throws Exception {
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertEquals(ALREADY_UP_TO_DATE, result.getStatus());
  }

  @Test
  public void mergeWhenHeadIsAheadWithLocalChanges_theResultShouldBeAlreadyUpToDate() throws Exception {
    write(gfs.getPath("/some_file.txt"), someBytes());
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertEquals(ALREADY_UP_TO_DATE, result.getStatus());
  }

  @Test
  public void mergeWhenHeadIsAheadWithLocalChanges_theLocalChangesShouldBeKept() throws Exception {
    write(gfs.getPath("/test_file.txt"), someBytes());
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertTrue(exists(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void mergeWhenHeadIsAheadWithLocalChanges_theResultCommitShouldBeTheHeadOfLocalBranch() throws IOException {
    ObjectId expected = getHeadCommit(OURS, repo);
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertEquals(ALREADY_UP_TO_DATE, result.getStatus());
    assertEquals(expected, result.getCommit());
  }

}
