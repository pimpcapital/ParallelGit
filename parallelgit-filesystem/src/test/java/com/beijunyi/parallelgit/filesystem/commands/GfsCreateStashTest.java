package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.commands.GfsCreateStash.Result;
import com.beijunyi.parallelgit.filesystem.exceptions.UnsuccessfulOperationException;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static org.junit.Assert.*;

public class GfsCreateStashTest extends PreSetupGitFileSystemTest {

  @Test
  public void whenFileSystemIsClean_stashShouldBeUnsuccessful() throws IOException {
    Result result = stash(gfs).execute();
    assertFalse(result.isSuccessful());
  }

  @Test(expected = UnsuccessfulOperationException.class)
  public void whenStashIsUnsuccessful_getCommitShouldThrowUnsuccessfulOperationException() throws IOException {
    Result result = stash(gfs).execute();
    result.getCommit();
  }

  @Test
  public void whenFileSystemIsClean_getCommitFrom() throws IOException {
    Result result = stash(gfs).execute();
    assertFalse(result.isSuccessful());
  }


  @Test
  public void afterChangesAreStashed_fileSystemShouldBecomeClean() throws IOException {
    writeSomeFileToGfs();
    stash(gfs).execute();
    assertFalse(isDirty(gfs));
  }

  @Test
  public void getParentCountOfTheStashCommit_shouldReturnTwo() throws IOException {
    writeSomeFileToGfs();
    Result result = stash(gfs).execute();
    RevCommit stash = result.getCommit();
    assertEquals(2, stash.getParentCount());
  }

  @Test
  public void getFirstParentOfTheStashCommit_shouldReturnTheHeadCommit() throws IOException {
    writeSomeFileToGfs();
    RevCommit head = gfs.getStatusProvider().commit();
    Result result = stash(gfs).execute();
    RevCommit stash = result.getCommit();
    assertEquals(head, stash.getParent(0));
  }

  @Test
  public void getSecondParentOfTheStashCommit_shouldReturnTheIndexCommitWhichHasTheSameTreeAsTheStashCommit() throws IOException {
    writeSomeFileToGfs();
    Result result = stash(gfs).execute();
    RevCommit stash = result.getCommit();
    RevCommit indexCommit = CommitUtils.getCommit(stash.getParent(1), repo);
    assertEquals(stash.getTree(), indexCommit.getTree());
  }

  @Test
  public void getTreeOfStashCommit_shouldContainTheStashedChanges() throws IOException {
    byte[] expected = "new file".getBytes();
    writeToGfs("/test_file.txt", expected);
    Result result = stash(gfs).execute();
    RevCommit stash = result.getCommit();
    ObjectId tree = stash.getTree();
    byte[] actual = TreeUtils.readFile("/test_file.txt", tree, repo).getData();
    assertArrayEquals(expected, actual);
  }

}
