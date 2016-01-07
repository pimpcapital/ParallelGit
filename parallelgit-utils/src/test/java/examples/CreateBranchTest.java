package examples;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CreateBranchTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    writeSomethingToCache();
    commitToMaster();
  }

  @Test
  public void createBranchFromAnotherBranch() throws IOException {
    BranchUtils.createBranch("my_branch", "master", repo);                     // create branch

    // check
    assertTrue(BranchUtils.branchExists("my_branch", repo));                   // "my_branch" exists
    assertEquals(CommitUtils.getCommit("master", repo),                        // the heads of the two branches equal
                  CommitUtils.getCommit("my_branch", repo));
  }

  @Test
  public void createBranchFromArbitraryCommit() throws IOException {
    AnyObjectId commit = repo.resolve("master");                               // get the head commit of "master"
    BranchUtils.createBranch("my_branch", commit, repo);                       // create branch

    // check
    assertTrue(BranchUtils.branchExists("my_branch", repo));                   // "my_branch" exists
    assertEquals(commit, CommitUtils.getCommit("my_branch", repo));            // the head commit equals to the input commit
  }

}
