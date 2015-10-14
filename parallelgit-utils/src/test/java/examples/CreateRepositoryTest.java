package examples;

import java.io.File;
import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CreateRepositoryTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepositoryDir();
  }

  @Test
  public void createBareRepository() throws IOException {
    Repository repo = RepositoryUtils.createRepository(repoDir);

    // check
    assertTrue(repo.isBare());
    assertEquals(repoDir, repo.getDirectory());
    // repo.getWorkTree(); -> NoWorkTreeException
  }

  @Test
  public void createNonBareRepository() throws IOException {
    Repository repo = RepositoryUtils.createRepository(repoDir, false);

    // check
    assertFalse(repo.isBare());
    assertEquals(repoDir, repo.getWorkTree());
    assertEquals(new File(repoDir, ".git"), repo.getDirectory());
  }

}
