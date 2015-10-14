package examples;

import java.io.File;
import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OpenRepositoryTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepositoryDir();
  }

  @Test
  public void openBareRepository() throws IOException {
    RepositoryUtils.createRepository(repoDir);

    Repository repo = RepositoryUtils.openRepository(repoDir);

    // check
    assertTrue(repo.isBare());
    assertEquals(repoDir, repo.getDirectory());
    // repo.getWorkTree(); -> NoWorkTreeException
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
