package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.commands.GfsCreateStash.Result;
import com.beijunyi.parallelgit.filesystem.exceptions.UnsuccessfulOperationException;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.createStash;
import static org.junit.Assert.*;

public class GfsCreateStashTest extends PreSetupGitFileSystemTest {

  @Test
  public void whenFileSystemIsClean_stashShouldBeUnsuccessful() throws IOException {
    Result result = createStash(gfs).execute();
    assertFalse(result.isSuccessful());
  }

  @Test(expected = UnsuccessfulOperationException.class)
  public void whenStashIsUnsuccessful_getCommitShouldThrowUnsuccessfulOperationException() throws IOException {
    Result result = createStash(gfs).execute();
    result.getCommit();
  }

  @Test
  public void stashWithWorkingDirectoryMessage_theWorkingDirectoryCommitMessageShouldContainTheSpecifiedMessage() throws IOException {
    writeSomethingToGfs();
    String message = "test working directory message";
    RevCommit workDirCommit = createStash(gfs).workingDirectoryMessage(message).execute().getCommit();
    assertTrue(workDirCommit.getFullMessage().equals(message));
  }

  @Test
  public void getParentCountOfTheStashCommit_shouldReturnTwo() throws IOException {
    writeSomethingToGfs();
    Result result = createStash(gfs).execute();
    RevCommit workDirCommit = result.getCommit();
    assertEquals(2, workDirCommit.getParentCount());
  }

  @Test
  public void getFirstParentOfTheWorkDirCommitCommit_shouldReturnTheHeadCommit() throws IOException {
    writeSomethingToGfs();
    RevCommit head = gfs.getStatusProvider().commit();
    Result result = createStash(gfs).execute();
    RevCommit workDirCommit = result.getCommit();
    assertEquals(head, workDirCommit.getParent(0));
  }

  @Test
  public void getSecondParentOfTheWorkDirCommitCommit_shouldReturnTheIndexCommitWhichHasTheSameTreeAsTheStashCommit() throws IOException {
    writeSomethingToGfs();
    Result result = createStash(gfs).execute();
    RevCommit workDirCommit = result.getCommit();
    RevCommit indexCommit = CommitUtils.getCommit(workDirCommit.getParent(1), repo);
    assertEquals(workDirCommit.getTree(), indexCommit.getTree());
  }

  @Test
  public void stashWithIndexMessage_theSecondParentOfTheWorkDirCommitMessageShouldContainTheSpecifiedMessage() throws IOException {
    writeSomethingToGfs();
    String message = "test index message";
    RevCommit stash = createStash(gfs).indexMessage(message).execute().getCommit();
    assertTrue(CommitUtils.getCommit(stash.getParent(1), repo).getFullMessage().equals(message));
  }

  @Test
  public void stashWithCommitter_bothWorkingDirectoryCommitAndIndexCommitShouldBeCommittedByTheSpecifiedCommitter() throws IOException {
    writeSomethingToGfs();
    PersonIdent committer = somePersonIdent();
    RevCommit workDirCommit = createStash(gfs).committer(committer).execute().getCommit();
    assertEquals(committer, workDirCommit.getCommitterIdent());
    RevCommit indexCommit = CommitUtils.getCommit(workDirCommit.getParent(1), repo);
    assertEquals(committer, workDirCommit.getCommitterIdent());
    assertEquals(committer, indexCommit.getCommitterIdent());
  }

  @Test
  public void getTreeOfWorkDirCommit_shouldContainTheStashedChanges() throws IOException {
    byte[] expected = someBytes();
    writeToGfs("/test_file.txt", expected);
    Result result = createStash(gfs).execute();
    RevCommit stash = result.getCommit();
    ObjectId tree = stash.getTree();
    byte[] actual = TreeUtils.readFile("/test_file.txt", tree, repo).getData();
    assertArrayEquals(expected, actual);
  }

}
