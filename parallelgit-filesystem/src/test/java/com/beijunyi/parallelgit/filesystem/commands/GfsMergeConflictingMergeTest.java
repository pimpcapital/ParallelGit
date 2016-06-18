package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.ParallelGitMergeTest;
import com.beijunyi.parallelgit.filesystem.merge.MergeNote;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Result;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Status.CONFLICTING;
import static com.beijunyi.parallelgit.utils.BranchUtils.getHeadCommit;
import static org.junit.Assert.*;

public class GfsMergeConflictingMergeTest extends AbstractGitFileSystemTest implements ParallelGitMergeTest {

  private GitFileSystem gfs;

  @Before
  public void setUp() throws IOException {
    initRepository();
    writeToCache("/test_file.txt", "base stuff");
    AnyObjectId base = commit();
    writeToCache("/test_file.txt", "base stuff + some stuff");
    commitToBranch(OURS, base);
    writeToCache("/test_file.txt", "base stuff + other stuff");
    commitToBranch(THEIRS, base);
    gfs = newFileSystem(OURS, repo);
  }

  @Test
  public void whenSourceBranchHasConflictingFile_theStatusShouldBeConflicting() throws IOException {
    Result result = merge(gfs).source(THEIRS).execute();
    assertEquals(CONFLICTING, result.getStatus());
  }

  @Test
  public void whenSourceBranchHasConflictingFile_resultShouldContainTheConflictingFile() throws IOException {
    Result result = merge(gfs).source(THEIRS).execute();
    assertTrue(result.getConflicts().containsKey("/test_file.txt"));
  }

  @Test
  public void whenSourceBranchHasConflictingFile_theConflictingFileShouldBeFormatted() throws IOException {
    merge(gfs).source(THEIRS).execute();
    assertEquals("<<<<<<< refs/heads/ours\n" +
                 "base stuff + some stuff\n" +
                 "=======\n" +
                 "base stuff + other stuff\n" +
                 ">>>>>>> refs/heads/theirs\n", readAsString(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void whenConflictIsEncountered_mergeNoteShouldHaveTheSourceBranchHead() throws IOException {
    merge(gfs).source(THEIRS).execute();
    MergeNote note = gfs.getStatusProvider().mergeNote();

    assertNotNull(note);
    assertEquals(getHeadCommit(THEIRS, repo), note.getSource());
  }

  @Test
  public void whenConflictIsEncountered_mergeNoteShouldHaveMergeConflictMessage() throws IOException {
    merge(gfs).source(THEIRS).execute();
    MergeNote note = gfs.getStatusProvider().mergeNote();

    assertNotNull(note);
    assertNotNull(note.getMessage());
  }

  @Test
  public void squashConflictingBranch_mergeNoteShouldHaveNoSourceCommit() throws IOException {
    writeToCache("/test_file.txt", "base stuff + other stuff + some more stuff");
    commitToBranch(THEIRS);
    merge(gfs).source(THEIRS).squash(true).execute();
    MergeNote note = gfs.getStatusProvider().mergeNote();

    assertNotNull(note);
    assertNull(note.getSource());
  }

  @Test
  public void squashConflictingBranch_mergeNoteShouldHaveMergeConflictMessage() throws IOException {
    writeToCache("/test_file.txt", "base stuff + other stuff + some more stuff");
    commitToBranch(THEIRS);
    merge(gfs).source(THEIRS).squash(true).execute();
    MergeNote note = gfs.getStatusProvider().mergeNote();

    assertNotNull(note);
    assertNotNull(note.getMessage());
  }



}
