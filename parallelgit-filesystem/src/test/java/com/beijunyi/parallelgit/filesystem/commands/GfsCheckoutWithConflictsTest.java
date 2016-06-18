package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.io.GfsCheckoutConflict;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

import static org.eclipse.jgit.lib.Constants.encode;
import static org.junit.Assert.*;

public class GfsCheckoutWithConflictsTest extends PreSetupGitFileSystemTest {

  @Test
  public void checkoutWhenLocalChangesConflictWithTarget_resultShouldBeUnsuccessful() throws IOException {
    writeToCache("conflicting_file.txt", "version A");
    commitToBranch("test_branch");
    writeToGfs("conflicting_file.txt", "version B");
    GfsCheckout.Result result = Gfs.checkout(gfs).target("test_branch").execute();

    assertTrue(result.hasConflicts());
    assertFalse(result.isSuccessful());
  }

  @Test
  public void checkoutWhenConflictsEncountered_fileSystemHeadShouldNotChange() throws IOException {
    ObjectId head = status.commit();
    writeToCache("conflicting_file.txt", "version A");
    commitToBranch("test_branch");
    writeToGfs("conflicting_file.txt", "version B");
    GfsCheckout.Result result = Gfs.checkout(gfs).target("test_branch").execute();

    assertTrue(result.hasConflicts());
    assertFalse(result.isSuccessful());
    assertEquals(head, status.commit());
  }

  @Test
  public void checkoutWhenConflictsEncountered_localFilesShouldNotChange() throws IOException {
    writeToCache("conflicting_file.txt", "version A");
    commitToBranch("test_branch");
    writeToGfs("conflicting_file.txt", "version B");
    GfsCheckout.Result result = Gfs.checkout(gfs).target("test_branch").execute();

    assertTrue(result.hasConflicts());
    assertFalse(result.isSuccessful());
    assertEquals("version B", readAsString(gfs.getPath("/conflicting_file.txt")));
  }

  @Test
  public void checkoutWhenConflictsEncountered_resultShouldHaveTheConflictingFilePath() throws IOException {
    writeToCache("conflicting_file.txt", "version A");
    commitToBranch("test_branch");
    writeToGfs("conflicting_file.txt", "version B");
    GfsCheckout.Result result = Gfs.checkout(gfs).target("test_branch").execute();

    assertTrue(result.hasConflicts());
    assertFalse(result.isSuccessful());
    assertTrue(result.getConflicts().containsKey("/conflicting_file.txt"));
  }

  @Test
  public void checkoutWhenConflictsEncountered_resultShouldHaveAllConflictingVersions() throws IOException {
    ObjectId baseVersion = writeToCache("conflicting_file.txt", "version BASE");
    ObjectId baseCommit = commitToBranch("test_branch");
    ObjectId theirsVersion = writeToCache("conflicting_file.txt", "version THEIRS");
    commitToBranch("test_branch");

    Gfs.checkout(gfs).target(baseCommit.name()).execute();
    byte[] ours = encode("version OURS");
    writeToGfs("conflicting_file.txt", ours);
    GfsCheckout.Result result = Gfs.checkout(gfs).target("test_branch").execute();

    assertTrue(result.hasConflicts());
    assertFalse(result.isSuccessful());
    GfsCheckoutConflict conflict = result.getConflicts().get("/conflicting_file.txt");

    assertEquals(baseVersion, conflict.getHead().getId());
    assertEquals(theirsVersion, conflict.getTarget().getId());
    assertEquals(calculateBlobId(ours), conflict.getWorktree().getId());
  }

  @Test
  public void checkoutWithForceOption_conflictingFilesShouldBeOverwritten() throws IOException {
    writeToCache("conflicting_file.txt", "version A");
    commitToBranch("test_branch");
    writeToGfs("conflicting_file.txt", "version B");
    GfsCheckout.Result result = Gfs.checkout(gfs).target("test_branch").force(true).execute();

    assertTrue(result.isSuccessful());
    assertEquals("version A", readAsString(gfs.getPath("/conflicting_file.txt")));
  }


}
