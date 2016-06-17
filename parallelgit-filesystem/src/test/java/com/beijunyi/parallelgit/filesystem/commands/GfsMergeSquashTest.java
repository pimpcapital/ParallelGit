package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.ParallelGitMergeTest;
import com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Result;
import com.beijunyi.parallelgit.filesystem.merge.MergeNote;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static java.nio.file.Files.exists;
import static org.junit.Assert.*;

public class GfsMergeSquashTest extends AbstractParallelGitTest implements ParallelGitMergeTest {

  private GitFileSystem gfs;

  @Before
  public void setUp() throws IOException {
    initRepository();
    ObjectId base = commit();
    clearCache();
    writeToCache("/test_file.txt", "OUR VERSION");
    commitToBranch(OURS, base);
    clearCache();
    writeToCache("/their_file1.txt");
    commitToBranch(THEIRS, base);
    writeToCache("/their_file2.txt");
    commitToBranch(THEIRS);
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
  public void mergeWithSquashOption_getCommitShouldReturnNull() throws IOException {
    Result result = merge(gfs).source(THEIRS).squash(true).execute();

    assertTrue(result.isSuccessful());
    assertNull(result.getCommit());
  }

  @Test
  public void mergeWithSquashOption_headCommitShouldRemainTheSame() throws IOException {
    ObjectId head = gfs.getStatusProvider().commit();
    Result result = merge(gfs).source(THEIRS).squash(true).execute();

    assertTrue(result.isSuccessful());
    assertEquals(head, gfs.getStatusProvider().commit());
  }

  @Test
  public void mergeWithSquashOption_filesFromTheSquashedCommitsShouldExistInTheFileSystem() throws IOException {
    Result result = merge(gfs).source(THEIRS).squash(true).execute();

    assertTrue(result.isSuccessful());
    assertTrue(exists(gfs.getPath("/their_file1.txt")));
    assertTrue(exists(gfs.getPath("/their_file2.txt")));
  }

  @Test
  public void mergeWithSquashOption_fileSystemShouldHaveMergeNoteWithMessageAndNoSourceCommit() throws IOException {
    Result result = merge(gfs).source(THEIRS).squash(true).execute();
    MergeNote note = gfs.getStatusProvider().mergeNote();

    assertTrue(result.isSuccessful());
    assertNotNull(note);
    assertNull(note.getSource());
    assertNotNull(note.getMessage());
  }

  @Test
  public void mergeConflictingBranchWithSquashOption_fileSystemShouldHaveMergeNoteWithMessageAndNoSourceCommit() throws IOException {
    writeToCache("/test_file.txt", "THEIR VERSION");
    commitToBranch(THEIRS);
    Result result = merge(gfs).source(THEIRS).squash(true).execute();
    MergeNote note = gfs.getStatusProvider().mergeNote();

    assertFalse(result.isSuccessful());
    assertNotNull(note);
    assertNull(note.getSource());
    assertNotNull(note.getMessage());
  }

}
