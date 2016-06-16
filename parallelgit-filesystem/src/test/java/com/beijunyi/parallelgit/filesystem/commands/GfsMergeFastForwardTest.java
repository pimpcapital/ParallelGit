package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.ParallelGitMergeTest;
import com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Result;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Status.*;
import static com.beijunyi.parallelgit.utils.BranchUtils.getHeadCommit;
import static java.nio.file.Files.*;
import static org.junit.Assert.*;

public class GfsMergeFastForwardTest extends AbstractParallelGitTest implements ParallelGitMergeTest {


  private GitFileSystem gfs;

  @Before
  public void setUp() throws IOException {
    initRepository();
    ObjectId base = commit();
    ObjectId ours = commitToBranch(OURS, base);
    writeToCache("/their_file.txt");
    commitToBranch(THEIRS, ours);
    gfs = newFileSystem(OURS, repo);
  }

  @After
  public void tearDown() throws IOException {
    if(gfs != null) {
      gfs.close();
      gfs = null;
    }
  }

  @Test
  public void mergeWhenHeadIsBehindSourceBranch_theStatusShouldBeFastForward() throws IOException {
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertEquals(FAST_FORWARD, result.getStatus());
  }

  @Test
  public void mergeWhenHeadIsBehindWithNonConflictingLocalChanges_theStatusShouldBeFastForward() throws IOException {
    write(gfs.getPath("/some_file.txt"), someBytes());
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertEquals(FAST_FORWARD, result.getStatus());
  }

  @Test
  public void whenFastForwardSucceedWithNewCommit_theResultCommitShouldBeTheHeadOfSourceBranch() throws IOException {
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertEquals(FAST_FORWARD, result.getStatus());
    assertEquals(getHeadCommit(THEIRS, repo), result.getCommit());
  }

  @Test
  public void mergeWhenHeadIsBehindWithNonConflictingLocalChanges_localChangesShouldBeKept() throws IOException {
    write(gfs.getPath("/test_file.txt"), someBytes());
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertTrue(exists(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void mergeWhenHeadIsBehindWithConflictingLocalChanges_theStatusShouldBeCheckoutConflict() throws IOException {
    write(gfs.getPath("/their_file.txt"), someBytes());
    Result result = merge(gfs).source(THEIRS).execute();

    assertFalse(result.isSuccessful());
    assertEquals(CHECKOUT_CONFLICT, result.getStatus());
  }

  @Test
  public void mergeWhenHeadIsBehindWithSquashOption_theResultShouldBeForwardSquashed() throws IOException {
    clearCache();
    writeSomethingToCache();
    commitToBranch(THEIRS);
    Result result = merge(gfs).source(THEIRS).squash(true).execute();

    assertTrue(result.isSuccessful());
    assertNull(result.getCommit());
    assertEquals(FAST_FORWARD_SQUASHED, result.getStatus());
  }



}
