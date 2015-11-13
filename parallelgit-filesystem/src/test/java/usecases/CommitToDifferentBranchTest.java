package usecases;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommitToDifferentBranchTest extends PreSetupGitFileSystemTest {

  @Test
  public void setBranchAndCommit_theHeadOfTheSpecifiedBranchShouldEqualToTheNewCommit() throws IOException {
    writeSomeFileToGfs();
    gfs.setBranch("test_branch");
    RevCommit commit = Gfs.commit(gfs).execute();
    assertEquals(CommitUtils.getCommit("test_branch", repo), commit);
  }

  @Test
  public void setBranchToNullAndCommit_theHeadOfThePreviousBranchShouldNotChange() throws IOException {
    String previousBranch = gfs.getBranch();
    assert previousBranch != null;
    RevCommit branchHead = CommitUtils.getCommit(previousBranch, repo);
    writeSomeFileToGfs();
    gfs.setBranch(null);
    Gfs.commit(gfs).execute();
    assertEquals(branchHead, CommitUtils.getCommit(previousBranch, repo));
  }
}
