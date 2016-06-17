package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.ParallelGitMergeTest;
import com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Result;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Status.MERGED;
import static java.nio.file.Files.readAllBytes;
import static org.junit.Assert.assertEquals;

public class GfsMergeAutoMergeTest extends AbstractParallelGitTest implements ParallelGitMergeTest {

  private GitFileSystem gfs;

  @Before
  public void setUp() throws IOException {
    initRepository();
    writeToCache("/test_file.txt", "a\nb\nc\nd\ne");
    AnyObjectId base = commit();
    writeToCache("/test_file.txt", "a\nB\nc\nd\ne");
    commitToBranch(OURS, base);
    writeToCache("/test_file.txt", "a\nb\nc\nD\nd\ne");
    commitToBranch(THEIRS, base);
    gfs = newFileSystem(OURS, repo);
  }

  @After
  public void tearDown() throws IOException {
    if(gfs != null) {
      gfs.close();
      gfs = null;
    }
  }

  @Test
  public void whenSourceBranchHasAutoMergeableFile_statusShouldBeMerged() throws IOException {
    Result result = merge(gfs).source(THEIRS).execute();
    assertEquals(MERGED, result.getStatus());
  }

  @Test
  public void whenSourceBranchHasAutoMergeableFile_theFileShouldHaveMergedContentAfterTheOperation() throws IOException {
    merge(gfs).source(THEIRS).execute();
    assertEquals("a\nB\nc\nD\nd\ne", new String(readAllBytes(gfs.getPath("/test_file.txt"))));
  }

}
