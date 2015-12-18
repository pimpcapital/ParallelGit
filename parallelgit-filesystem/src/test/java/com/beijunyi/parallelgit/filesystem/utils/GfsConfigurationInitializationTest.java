package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.Assert;
import org.junit.Test;

public class GfsConfigurationInitializationTest extends AbstractParallelGitTest {

  @Test
  public void initWithRepository() throws IOException {
    initRepository();
    GfsConfiguration cfg = GfsConfiguration.repo(repo);
    Assert.assertEquals(repo, cfg.repository());
  }

}
