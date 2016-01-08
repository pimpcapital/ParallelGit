package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.Before;
import org.junit.Test;

public class GfsTreeIteratorTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void a() throws IOException {
    writeMultipleToCache();
  }

}
