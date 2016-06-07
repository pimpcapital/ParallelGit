package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsConfigurationInitializationTest extends AbstractParallelGitTest {

  @Test
  public void initWithRepository() throws IOException {
    initRepository();
    GfsConfiguration cfg = GfsConfiguration.repo(repo);
    assertEquals(repo, cfg.repository());
  }

}
