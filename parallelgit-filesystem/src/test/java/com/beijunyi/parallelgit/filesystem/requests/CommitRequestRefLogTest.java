package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.utils.RefHelper;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CommitRequestRefLogTest extends AbstractGitFileSystemTest {

  private String branch;

  @Before
  public void setupFileSystem() throws IOException {
    initRepository(false, false);
    initGitFileSystem();
    branch = gfs.getBranch();
  }

  @Test
  public void commitWithMessage_theLastRefLogShouldContainTheShortMessage() throws IOException {
    writeSomeFileToGfs();
    RevCommit commit = Requests.commit(gfs)
                         .message("expected message")
                         .execute();
    assert commit != null;
    Assert.assertTrue(repo.getReflogReader(RefHelper.getBranchRefName(branch)).getLastEntry().getComment().contains(commit.getShortMessage()));
  }

  @Test
  public void commitWithRefLog_theLastRefLogShouldBeTheSameAsTheSpecifiedRefLog() throws IOException {
    writeSomeFileToGfs();
    String reflog = "some custom reflog message";
    Requests.commit(gfs)
      .refLog("some custom reflog message")
      .execute();
    Assert.assertEquals(reflog, repo.getReflogReader(RefHelper.getBranchRefName(branch)).getLastEntry().getComment());
  }

}
