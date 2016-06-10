package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.commands.GfsCheckout.Result;
import com.beijunyi.parallelgit.filesystem.exceptions.NoBranchException;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

import static org.junit.Assert.*;

public class GfsCheckoutWithNoConflictTest extends PreSetupGitFileSystemTest {

  @Test
  public void checkoutWhenThereIsNoLocalChanges_fileSystemShouldRemainClean() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    Result result = Gfs.checkout(gfs).target("test_branch").execute();

    assertFalse(result.hasConflicts());
    assertTrue(result.isSuccessful());
    assertFalse(status.isDirty());
  }

  @Test
  public void checkoutWhenLocalChangesDoNotCauseConflict_localChangesShouldBeKept() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    writeToGfs("non_conflicting_file.txt");
    Result result = Gfs.checkout(gfs).target("test_branch").execute();

    assertFalse(result.hasConflicts());
    assertTrue(result.isSuccessful());
    assertTrue(Files.exists(gfs.getPath("non_conflicting_file.txt")));
  }

  @Test
  public void checkoutBranchWhenThereIsNoConflict_fileSystemShouldAttachToTargetBranch() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    Result result = Gfs.checkout(gfs).target("test_branch").execute();

    assertFalse(result.hasConflicts());
    assertTrue(result.isSuccessful());
    assertTrue(status.isAttached());
    assertEquals("test_branch", status.branch());
  }

  @Test
  public void checkoutBranchWithDetachOption_fileSystemShouldBecomeDetached() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    Result result = Gfs.checkout(gfs).target("test_branch").detach(true).execute();

    assertFalse(result.hasConflicts());
    assertTrue(result.isSuccessful());
    assertFalse(status.isAttached());
  }

  @Test
  public void checkoutBranchWithDetachOption_fileSystemHeadShouldBecomeTheTargetBranchHeadCommit() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    Result result = Gfs.checkout(gfs).target("test_branch").detach(true).execute();

    assertFalse(result.hasConflicts());
    assertTrue(result.isSuccessful());
    assertEquals(BranchUtils.getHeadCommit("test_branch", repo), status.commit());
  }

  @Test
  public void checkoutCommitWhenThereIsNoConflict_fileSystemShouldBecomeDetached() throws IOException {
    writeSomethingToCache();
    ObjectId commitId = commit();
    Result result = Gfs.checkout(gfs).target(commitId.name()).execute();

    assertFalse(result.hasConflicts());
    assertTrue(result.isSuccessful());
    assertFalse(status.isAttached());
  }

  @Test
  public void checkoutCommitWhenThereIsNoConflict_fileSystemHeadShouldBecomeTheTargetCommit() throws IOException {
    writeSomethingToCache();
    ObjectId commitId = commit();
    Result result = Gfs.checkout(gfs).target(commitId.name()).execute();

    assertFalse(result.hasConflicts());
    assertTrue(result.isSuccessful());
    assertEquals(commitId, status.commit());
  }

  @Test(expected = NoBranchException.class)
  public void checkoutWithNoTargetSpecified_shouldThrowNoBranchException() throws IOException {
    Gfs.checkout(gfs).execute();
  }


  @Test(expected = NoSuchBranchException.class)
  public void checkoutNonExistentBranch_shouldThrowNoSuchBranchException() throws IOException {
    Gfs.checkout(gfs).target("non_existent_branch").execute();
  }


}
