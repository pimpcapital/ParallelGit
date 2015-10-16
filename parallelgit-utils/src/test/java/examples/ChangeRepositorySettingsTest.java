package examples;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChangeRepositorySettingsTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
  }

  @Test
  public void changeDefaultCommitter() throws IOException {
    RepositoryUtils.setDefaultCommitter("example", "example@email.com", repo);

    // check
    PersonIdent defaultCommitter = new PersonIdent(repo);
    assertEquals("example", defaultCommitter.getName());
    assertEquals("example@email.com", defaultCommitter.getEmailAddress());
  }

}
