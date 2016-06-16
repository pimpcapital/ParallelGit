package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.ParallelGitMergeTest;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.merge.MergeStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Result;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Status.*;
import static com.beijunyi.parallelgit.utils.BranchUtils.getHeadCommit;
import static java.nio.file.Files.*;
import static org.junit.Assert.*;

public class GfsMergeGeneralCaseTest extends AbstractParallelGitTest implements ParallelGitMergeTest {

  private GitFileSystem gfs;

  @Before
  public void setUp() throws IOException {
    initRepository();
    AnyObjectId base = commit();
    writeToCache("/our_file.txt");
    commitToBranch(OURS, base);
    clearCache();
    writeToCache("/their_file.txt");
    commitToBranch(THEIRS, base);
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
  public void whenMergeSucceedWithNewCommit_theParentsAreTheHeadsOfCurrentAndSourceBranch() throws IOException {
    ObjectId ourHead = getHeadCommit(OURS, repo);
    ObjectId theirHead = getHeadCommit(THEIRS, repo);
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertNotNull(result.getCommit());
    assertEquals(ourHead, result.getCommit().getParent(0));
    assertEquals(theirHead, result.getCommit().getParent(1));
  }

  @Test
  public void mergeWhenSourceBranchHasNonConflictingChanges_theStatusShouldBeMerged() throws IOException {
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertEquals(MERGED, result.getStatus());
  }

  @Test
  public void mergeWhenSourceBranchHasNonConflictingChanges_theChangesShouldBeMergedIn() throws IOException {
    clearCache();
    writeToCache("/test_file.txt");
    commitToBranch(THEIRS);
    Result result = merge(gfs).source(THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertTrue(exists(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void mergeWhenSourceBranchIsNotUpToDateWithFastForwardOnlyOption_theStatusShouldBeAborted() throws IOException {
    Result result = merge(gfs).source(THEIRS).fastForwardOnly(true).execute();

    assertFalse(result.isSuccessful());
    assertEquals(ABORTED, result.getStatus());
  }

  @Test
  public void mergeWithSpecifiedCommitter_theResultCommitShouldBeCommittedByThisPerson() throws IOException {
    PersonIdent expected = somePersonIdent();
    Result result = merge(gfs).source(THEIRS).committer(expected).execute();

    assertTrue(result.isSuccessful());
    assertEquals(expected, gfs.getStatusProvider().commit().getCommitterIdent());
    assertEquals(expected, getHeadCommit(OURS, repo).getCommitterIdent());
  }

  @Test
  public void mergeWithSpecifiedMessage_theResultCommitShouldContainThisMessage() throws IOException {
    String expected = someCommitMessage();
    Result result = merge(gfs).source(THEIRS).message(expected).execute();

    assertTrue(result.isSuccessful());
    assertEquals(expected, gfs.getStatusProvider().commit().getFullMessage());
    assertEquals(expected, getHeadCommit(OURS, repo).getFullMessage());
  }

  @Test
  public void mergeWithSpecifiedStrategy_theBehaviourShouldChangeAccordingly() throws IOException {
    clearCache();
    byte[] expected = someBytes();
    writeToCache("/our_file.txt", expected);
    commitToBranch(THEIRS);
    Result result = merge(gfs).source(THEIRS).strategy(MergeStrategy.THEIRS).execute();

    assertTrue(result.isSuccessful());
    assertArrayEquals(expected, readAllBytes(gfs.getPath("/our_file.txt")));
  }

  @Test(expected = NoBranchException.class)
  public void mergeWhenFileSystemIsNotAttached_shouldThrowNoBranchException() throws IOException {
    gfs = newFileSystem(getHeadCommit(THEIRS, repo), repo);
    merge(gfs).source(THEIRS).execute();
  }

}
