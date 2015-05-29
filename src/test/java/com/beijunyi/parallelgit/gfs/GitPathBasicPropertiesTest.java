package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.file.WatchEvent;

import org.junit.Before;
import org.junit.Test;

public class GitPathBasicPropertiesTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
    preventDestroyFileSystem();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void toFileTest() {
    root.toFile();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void registerWatcherTest() {
    root.register(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void registerWatcherWithModifierTest() {
    root.register(null, new WatchEvent.Kind<?>[0], new WatchEvent.Modifier[0]);
  }
}
