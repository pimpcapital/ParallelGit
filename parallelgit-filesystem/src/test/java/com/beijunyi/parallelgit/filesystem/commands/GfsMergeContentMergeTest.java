package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.ParallelGitMergeTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Result;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMerge.Status.CONFLICTING;
import static com.beijunyi.parallelgit.utils.BranchUtils.createBranch;
import static java.nio.file.Files.readAllBytes;
import static org.junit.Assert.*;

public class GfsMergeContentMergeTest extends AbstractParallelGitTest implements ParallelGitMergeTest {

  private GitFileSystem gfs;

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @After
  public void tearDown() throws IOException {
    if(gfs != null) {
      gfs.close();
      gfs = null;
    }
  }

  @Test
  public void whenSourceBranchHasAutoMergeableFile_theFileShouldHaveMergedContentAfterTheOperation() throws IOException {
    writeToCache("/test_file.txt", "a\nb\nc\nd\ne");
    AnyObjectId base = commit();
    clearCache();
    writeToCache("/test_file.txt", "a\nB\nc\nd\ne");
    AnyObjectId ours = commit(base);
    clearCache();
    writeToCache("/test_file.txt", "a\nb\nc\nD\nd\ne");
    AnyObjectId theirs = commit(base);
    prepareBranches(ours, theirs);
    try(GitFileSystem gfs = prepareFileSystem()) {
      merge(gfs).source("theirs").execute();
      assertArrayEquals("a\nB\nc\nD\nd\ne".getBytes(), readAllBytes(gfs.getPath("/test_file.txt")));
    }
  }

  @Test
  public void whenSourceBranchHasConflictingFile_theResultShouldBeConflicting() throws IOException {
    writeToCache("/test_file.txt", "some stuff");
    AnyObjectId base = commit();
    clearCache();
    writeToCache("/test_file.txt", "other stuff");
    AnyObjectId ours = commit(base);
    clearCache();
    writeToCache("/test_file.txt", "completely different stuff");
    AnyObjectId theirs = commit(base);
    prepareBranches(ours, theirs);
    Result result = mergeBranches();
    assertEquals(CONFLICTING, result.getStatus());
  }

  @Test
  public void whenSourceBranchHasConflictingFile_theConflictingFileShouldBeFormatted() throws IOException {
    writeToCache("/test_file.txt", "some stuff");
    AnyObjectId base = commit();
    clearCache();
    writeToCache("/test_file.txt", "other stuff");
    AnyObjectId ours = commit(base);
    clearCache();
    writeToCache("/test_file.txt", "completely different stuff");
    AnyObjectId theirs = commit(base);
    prepareBranches(ours, theirs);
    try(GitFileSystem gfs = prepareFileSystem()) {
      merge(gfs).source("theirs").execute();
      assertEquals("<<<<<<< refs/heads/ours\n" +
                   "other stuff\n" +
                   "=======\n" +
                   "completely different stuff\n" +
                   ">>>>>>> refs/heads/theirs\n", new String(readAllBytes(gfs.getPath("/test_file.txt"))));
    }
  }


  private void prepareBranches(AnyObjectId ours, AnyObjectId theirs) throws IOException {
    createBranch("ours", ours, repo);
    createBranch("theirs", theirs, repo);
  }

  @Nonnull
  private GitFileSystem prepareFileSystem() throws IOException {
    return newFileSystem("ours", repo);
  }

  @Nonnull
  private Result mergeBranches() throws IOException {
    try(GitFileSystem gfs = prepareFileSystem()) {
      return merge(gfs).source("theirs").execute();
    }
  }

}
