package examples;

import java.io.File;
import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.eclipse.jgit.lib.Constants.DOT_GIT;
import static org.junit.Assert.*;

public class CreateRepositoryTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepositoryDir();
  }

  @Test
  public void createBareRepository() throws IOException {
    Repository repo = RepositoryUtils.createRepository(repoDir);               // create a bare repository

    // check
    assertTrue(repo.isBare());                                                 // the repository is bare
    assertEquals(repoDir, repo.getDirectory());                                // the directory equals repoDir
    // repo.getWorkTree(); -> NoWorkTreeException
  }

  @Test
  public void createNonBareRepository() throws IOException {
    Repository repo = RepositoryUtils.createRepository(repoDir, false);         // create a non-bare repository

    // check
    assertFalse(repo.isBare());                                                 // the repository is not bare
    assertEquals(repoDir, repo.getWorkTree());                                  // the wortree equals repoDir
    assertEquals(new File(repoDir, DOT_GIT), repo.getDirectory());              // the directory equals repoDir/.git
  }

}
