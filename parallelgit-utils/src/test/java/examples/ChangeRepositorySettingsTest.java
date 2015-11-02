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
    RepositoryUtils.setDefaultCommitter("example", "example@email.com", repo); // set default committer

    // check
    PersonIdent defaultCommitter = new PersonIdent(repo);                      // create a new PersonIdent
    assertEquals("example", defaultCommitter.getName());                       // the name equals input name
    assertEquals("example@email.com", defaultCommitter.getEmailAddress());     // the email equals input email
  }

}
