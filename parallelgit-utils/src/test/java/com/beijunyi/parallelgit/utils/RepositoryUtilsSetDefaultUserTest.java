package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RepositoryUtilsSetDefaultUserTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void setDefaultUser_newPersonIdentInstanceShouldHaveTheSpecifiedUserNameAndEmail() throws IOException {
    String name = "test_user";
    String email = "test@user.com";
    RepositoryUtils.setDefaultUser(name, email, repo);
    PersonIdent actual = new PersonIdent(repo);
    Assert.assertEquals(name, actual.getName());
    Assert.assertEquals(email, actual.getEmailAddress());
  }


}
