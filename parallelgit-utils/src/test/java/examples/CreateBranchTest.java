package examples;

import java.io.File;
import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CreateBranchTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    writeSomeFileToCache();
    commitToMaster();
  }

  @Test
  public void createBranch() throws IOException {
    BranchUtils.createBranch("my_branch", "master", repo);

    // check
    assertTrue(BranchUtils.branchExists("my_branch", repo));
    assertEquals(CommitUtils.getCommit("master", repo), CommitUtils.getCommit("my_branch", repo));
  }

  @Test
  public void createNonBareRepository() throws IOException {
    RepositoryUtils.createRepository(repoDir, false);

    Repository repo = RepositoryUtils.openRepository(repoDir);

    // check
    assertFalse(repo.isBare());
    assertEquals(repoDir, repo.getWorkTree());
    assertEquals(new File(repoDir, ".git"), repo.getDirectory());
  }

}
