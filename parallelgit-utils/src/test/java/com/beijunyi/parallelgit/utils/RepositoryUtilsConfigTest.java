package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.RepositoryUtils.*;
import static org.junit.Assert.*;

public class RepositoryUtilsConfigTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initFileRepository(true);
  }

  @Test
  public void setDefaultCommitter_newPersonIdentInstanceShouldHaveTheSpecifiedUserNameAndEmail() throws IOException {
    String name = "test_user";
    String email = "test@user.com";
    RepositoryUtils.setDefaultCommitter(name, email, repo);
    PersonIdent actual = new PersonIdent(repo);
    assertEquals(name, actual.getName());
    assertEquals(email, actual.getEmailAddress());
  }

  @Test
  public void setRefLogEnabledToTrue_RefLogShouldBeEnabled() throws IOException {
    setRefLogEnabled(true, repo);
    assertTrue(isRefLogEnabled(repo));
  }

  @Test
  public void setRefLogEnabledToFalse_RefLogShouldBeDisabled() throws IOException {
    setRefLogEnabled(false, repo);
    assertFalse(isRefLogEnabled(repo));
  }


}
