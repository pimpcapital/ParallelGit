package examples;

import java.io.File;
import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.eclipse.jgit.lib.Constants.DOT_GIT;
import static org.junit.Assert.*;

public class OpenRepositoryTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepositoryDir();
  }

  @Test
  public void autoDetectAndOpenBareRepository() throws IOException {
    RepositoryUtils.createRepository(repoDir);                                 // prepare a bare repository

    Repository repo = RepositoryUtils.openRepository(repoDir);                 // open the repository

    // check
    assertEquals(repoDir, repo.getDirectory());                                // the directory equals repoDir
  }

  @Test
  public void autoDetectAndOpenNonBareRepository() throws IOException {
    RepositoryUtils.createRepository(repoDir);                                 // prepare a non-bare repository

    Repository repo = RepositoryUtils.openRepository(repoDir);                 // open the repository

    // check
    assertEquals(repoDir, repo.getWorkTree());                                 // the worktree equals repoDir
    assertEquals(new File(repoDir, DOT_GIT), repo.getWorkTree());              // the directory equals repoDir/.git
  }

}
