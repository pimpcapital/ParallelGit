package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;
import java.nio.file.Files;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.*;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static com.beijunyi.parallelgit.filesystem.commands.GfsMergeCommand.Result;
import static com.beijunyi.parallelgit.utils.BranchUtils.createBranch;
import static org.eclipse.jgit.api.MergeResult.MergeStatus.*;

public class GfsMergeCommandTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initRepository();
  }

  @Test
  public void whenHeadIsBehindSourceBranch_theResultShouldBeFastForward() throws Exception {
    AnyObjectId parentCommit = commit();
    prepareBranches(parentCommit, commit(parentCommit));
    Result result = mergeBranches();
    Assert.assertEquals(FAST_FORWARD, result.getStatus());
  }

  @Test
  public void whenHeadIsBehindWithUnstashedFile_theResultShouldBeFastForward() throws Exception {
    AnyObjectId parentCommit = commit();
    prepareBranches(parentCommit, commit(parentCommit));
    Result result;
    try(GitFileSystem gfs = prepareFileSystem()) {
      Files.write(gfs.getPath("/some_file.txt"), someBytes());
      result = merge(gfs).source("theirs").execute();
    }
    Assert.assertEquals(FAST_FORWARD, result.getStatus());
  }

  @Test
  public void whenHeadIsBehindWithUnstashedFile_theFileShouldExistInTheFileSystemAfterMerge() throws Exception {
    AnyObjectId parentCommit = commit();
    prepareBranches(parentCommit, commit(parentCommit));
    try(GitFileSystem gfs = prepareFileSystem()) {
      Files.write(gfs.getPath("/test_file.txt"), someBytes());
      merge(gfs).source("theirs").execute();
      Assert.assertTrue(Files.exists(gfs.getPath("/test_file.txt")));
    }
  }

  @Test
  public void whenSourceBranchHasNonConflictingFile_theResultShouldBeMerged() throws IOException {
    AnyObjectId base = commit();
    AnyObjectId ours = commit(base);
    writeToCache("/some_file.txt");
    AnyObjectId theirs = commit(base);
    prepareBranches(ours, theirs);
    Result result = mergeBranches();
    Assert.assertEquals(MERGED, result.getStatus());
  }

  @Test
  public void whenSourceBranchHasNonConflictingFile_theFileShouldExistAfterTheOperation() throws IOException {
    AnyObjectId base = commit();
    AnyObjectId ours = commit(base);
    writeToCache("/test_file.txt");
    AnyObjectId theirs = commit(base);
    prepareBranches(ours, theirs);
    try(GitFileSystem gfs = prepareFileSystem()) {
      merge(gfs).source("theirs").execute();
      Assert.assertTrue(Files.exists(gfs.getPath("/test_file.txt")));
    }
  }

  @Test
  public void whenSourceBranchHasAutoMergeableFile_theResultShouldBeMerged() throws IOException {
    writeToCache("/test_file.txt", "a\nb\nc\nd\ne");
    AnyObjectId base = commit();
    clearCache();
    writeToCache("/test_file.txt", "a\nB\nc\nd\ne");
    AnyObjectId ours = commit(base);
    clearCache();
    writeToCache("/test_file.txt", "a\nb\nc\nD\nd\ne");
    AnyObjectId theirs = commit(base);
    prepareBranches(ours, theirs);
    Result result = mergeBranches();
    Assert.assertEquals(MERGED, result.getStatus());
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
      Assert.assertArrayEquals("a\nB\nc\nD\nd\ne".getBytes(), Files.readAllBytes(gfs.getPath("/test_file.txt")));
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
    Assert.assertEquals(CONFLICTING, result.getStatus());
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
      Assert.assertArrayEquals(("<<<<<<< refs/heads/theirs\n" +
                                 "other stuff\n" +
                                 "=======\n" +
                                 "completely different stuff\n" +
                                 ">>>>>>> refs/heads/ours\n").getBytes(), Files.readAllBytes(gfs.getPath("/test_file.txt")));
    }
  }


  private void prepareBranches(@Nonnull AnyObjectId ours, @Nonnull AnyObjectId theirs) throws IOException {
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
