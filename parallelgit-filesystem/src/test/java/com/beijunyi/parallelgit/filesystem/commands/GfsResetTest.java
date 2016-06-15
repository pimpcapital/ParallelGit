package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.utils.BranchUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import static org.junit.Assert.*;

public class GfsResetTest extends PreSetupGitFileSystemTest {

  @Test
  public void resetWhenThereIsNoLocalChange_operationShouldBeSuccessful() throws IOException {
    GfsReset.Result result = Gfs.reset(gfs).execute();

    assertTrue(result.isSuccessful());
  }

  @Test
  public void resetWhenThereAreLocalChanges_changesShouldBeUndone() throws IOException {
    writeToGfs("/test_file.txt");
    GfsReset.Result result = Gfs.reset(gfs).execute();

    assertTrue(result.isSuccessful());
    assertFalse(Files.exists(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void resetWhenThereAreLocalChanges_fileSystemShouldBecomeClean() throws IOException {
    writeSomethingToGfs();
    GfsReset.Result result = Gfs.reset(gfs).execute();

    assertTrue(result.isSuccessful());
    assertFalse(status.isDirty());
  }

  @Test
  public void resetToRevisionWhenThereIsNoLocalChange_branchHeadShouldBecomeTheSpecifiedRevision() throws IOException {
    writeSomethingToCache();
    ObjectId expected = commit();
    GfsReset.Result result = Gfs.reset(gfs).revision(expected.getName()).execute();

    assertTrue(result.isSuccessful());
    assertFalse(status.isDirty());
    assertEquals(expected, status.commit());
    assertEquals(expected, BranchUtils.getHeadCommit(status.branch(), repo));
  }

  @Test
  public void resetToRevision_fileSystemRootTreeShouldBecomeTheSameAsRevisionRootTree() throws IOException {
    writeSomethingToCache();
    RevCommit revision = commit();
    writeSomethingToGfs();
    GfsReset.Result result = Gfs.reset(gfs).revision(revision.getName()).execute();

    assertTrue(result.isSuccessful());
    assertEquals(revision.getTree(), gfs.getFileStore().getRoot().getObjectId(false));
  }

  @Test
  public void resetWithSoftOption_fileSystemRootTreeShouldNotBeChanged() throws IOException {
    writeSomethingToCache();
    RevCommit revision = commit();
    writeSomethingToGfs();
    ObjectId before = gfs.getFileStore().getRoot().getObjectId(false);
    GfsReset.Result result = Gfs.reset(gfs).revision(revision.getName()).soft(true).execute();
    ObjectId after = gfs.getFileStore().getRoot().getObjectId(false);

    assertTrue(result.isSuccessful());
    assertEquals(before, after);
  }


  @Test
  public void resetToRevisionWhenThereAreLocalChanges_branchHeadShouldBecomeTheSpecifiedRevision() throws IOException {
    writeSomethingToCache();
    ObjectId expected = commit();
    writeSomethingToGfs();
    GfsReset.Result result = Gfs.reset(gfs).revision(expected.getName()).execute();

    assertTrue(result.isSuccessful());
    assertFalse(status.isDirty());
    assertEquals(expected, status.commit());
    assertEquals(expected, BranchUtils.getHeadCommit(status.branch(), repo));
  }

  @Test
  public void resetWithSoftOptionWhenThereAreLocalChanges_branchHeadShouldBecomeTheSpecifiedRevision() throws IOException {
    writeSomethingToCache();
    ObjectId expected = commit();
    writeSomethingToGfs();
    GfsReset.Result result = Gfs.reset(gfs).revision(expected.getName()).soft(true).execute();

    assertTrue(result.isSuccessful());
    assertEquals(expected, status.commit());
    assertEquals(expected, BranchUtils.getHeadCommit(status.branch(), repo));
  }


  @Test
  public void resetWithSoftOptionWhenThereAreLocalChanges_localChangeShouldBeKept() throws IOException {
    writeSomethingToCache();
    ObjectId expected = commit();
    writeToGfs("/test_file.txt");
    GfsReset.Result result = Gfs.reset(gfs).revision(expected.getName()).soft(true).execute();

    assertTrue(result.isSuccessful());
    assertTrue(Files.exists(gfs.getPath("/test_file.txt")));
    assertTrue(status.isDirty());
  }

  @Test
  public void resetWithHardOptionWhenThereIsMerge_mergeNoteShouldBeCleared() throws IOException {
    writeSomethingToCache();
    commitToBranch("some_branch");
    Gfs.merge(gfs).source("some_branch").commit(false).execute();
    GfsReset.Result result = Gfs.reset(gfs).hard(true).execute();

    assertTrue(result.isSuccessful());
    assertNull(status.mergeNote());
  }


}
