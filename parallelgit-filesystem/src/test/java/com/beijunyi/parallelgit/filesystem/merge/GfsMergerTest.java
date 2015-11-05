package com.beijunyi.parallelgit.filesystem.merge;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

public class GfsMergerTest extends AbstractParallelGitTest {

  private GfsMerger merger;

  @Before
  public void setUp() throws Exception {
    initRepository();
    merger = new GfsMerger(repo);
  }

  @Test
  public void testName() throws Exception {
    AnyObjectId base = commit(null);
    writeToCache("/file1.txt");
    AnyObjectId theirs = commit(base);
    clearCache();
    writeToCache("/file2.txt");
    AnyObjectId ours = commit(base);
    merger.merge(ours, theirs);
  }
}
