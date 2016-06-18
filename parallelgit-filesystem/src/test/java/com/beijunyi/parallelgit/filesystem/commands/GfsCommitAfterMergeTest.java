package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.ParallelGitMergeTest;
import com.beijunyi.parallelgit.filesystem.merge.MergeNote;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.newFileSystem;
import static com.beijunyi.parallelgit.utils.BranchUtils.getHeadCommit;
import static java.nio.file.Files.write;
import static org.junit.Assert.*;

public class GfsCommitAfterMergeTest extends AbstractParallelGitTest implements ParallelGitMergeTest {

  private GitFileSystem gfs;

  @Before
  public void setUp() throws IOException {
    initRepository();
    writeSomethingToCache();
    AnyObjectId base = commit();
    writeToCache("/test_file.txt", "OUR VERSION");
    commitToBranch(OURS, base);
    clearCache();
    writeSomethingToCache();
    commitToBranch(THEIRS, base);
    writeSomethingToCache();
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
  public void commitAfterMerge_resultCommitShouldHaveTheMessageSpecifiedInTheMergeNote() throws IOException {
    Gfs.merge(gfs).source(THEIRS).commit(false).execute();
    MergeNote note = gfs.getStatusProvider().mergeNote();
    RevCommit commit = Gfs.commit(gfs).execute().getCommit();

    assertNotNull(note);
    assertNotNull(commit);
    assertEquals(note.getMessage(), commit.getFullMessage());
  }

  @Test
  public void commitAfterMerge_resultCommitSecondParentShouldBeTheSourceCommitSpecifiedInTheMergeNote() throws IOException {
    Gfs.merge(gfs).source(THEIRS).commit(false).execute();
    MergeNote note = gfs.getStatusProvider().mergeNote();
    RevCommit commit = Gfs.commit(gfs).execute().getCommit();

    assertNotNull(note);
    assertNotNull(commit);
    assertEquals(note.getSource(), commit.getParent(1));
  }

  @Test
  public void commitAfterSquashMerge_resultCommitShouldHavePreviousHeadCommitAsOnlyParent() throws IOException {
    ObjectId previousHead = getHeadCommit(OURS, repo);
    Gfs.merge(gfs).source(THEIRS).squash(true).execute();
    RevCommit commit = Gfs.commit(gfs).execute().getCommit();

    assertEquals(1, commit.getParentCount());
    assertEquals(previousHead, commit.getParent(0));
  }

  @Test
  public void commitAfterConflictingMerge_resultCommitShouldHaveOneParent() throws IOException {
    writeToCache("/test_file.txt", "THEIR VERSION");
    commitToBranch(THEIRS);
    Gfs.merge(gfs).source(THEIRS).execute();
    MergeNote note = gfs.getStatusProvider().mergeNote();
    write(gfs.getPath("/test_file.txt"), Constants.encodeASCII("COMBINED VERSION"));
    RevCommit commit = Gfs.commit(gfs).execute().getCommit();

    assertNotNull(note);
    assertNotNull(commit);
    assertEquals(note.getSource(), commit.getParent(1));
  }

}
