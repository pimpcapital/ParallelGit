package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class GfsMergerConflictingMergeTest extends AbstractParallelGitTest {

  private GfsMerger merger;
  private AnyObjectId ours;
  private AnyObjectId theirs;

  @Before
  public void setUp() throws IOException {
    initRepository();
    merger = new GfsMerger(repo);
  }
  private void prepareFileWithConflictingModes(@Nonnull String conflictingFile) throws IOException {
    byte[] data = "some text data".getBytes();
    writeToCache(conflictingFile, data, FileMode.SYMLINK);
    AnyObjectId base = commit(null);

    clearCache();
    writeToCache(conflictingFile, data, FileMode.EXECUTABLE_FILE);
    ours = commit(base);

    clearCache();
    writeToCache(conflictingFile, data, FileMode.REGULAR_FILE);
    theirs = commit(base);
  }

  @Test
  public void mergeFileWithConflictingModes_shouldReturnFalse() throws IOException {
    prepareFileWithConflictingModes("/test_file.txt");
    assertFalse(merger.merge(ours, theirs));
  }

  @Test
  public void mergeFileWithConflictingModes_theConflictsShouldContainTheConflictingFile() throws IOException {
    prepareFileWithConflictingModes("/test_file.txt");
    assertFalse(merger.getConflicts().containsKey("/test_file.txt"));
  }

  @Test
  public void mergeFileWithConflictingData_() throws Exception {
    writeToCache("/test_file.txt", "some text data");
    RevCommit base = commit(null);

    clearCache();
    writeToCache("/test_file.txt", "some text data 1");
    RevCommit ours = commit(base);

    clearCache();
    writeToCache("/test_file.txt", "some text data 2");
    RevCommit theirs = commit(base);

  }

  @Test
  public void mergeFileConflictingWithDirectory_() throws Exception {
    RevCommit base = commit(null);

    clearCache();
    writeToCache("/test_file/file.txt");
    RevCommit ours = commit(base);

    clearCache();
    writeToCache("/test_file");
    RevCommit theirs = commit(base);

    Git git = new Git(repo);
    git.branchCreate().setName("test_branch").setStartPoint(ours).call();
    git.checkout().setName("test_branch").call();

    CherryPickResult result = git.cherryPick().include(theirs).call();

    System.currentTimeMillis();
  }

  @Test
  public void mergeDirectoryConflictingWithFile_() throws Exception {
    RevCommit base = commit(null);

    clearCache();
    writeToCache("/test_file");
    RevCommit ours = commit(base);

    clearCache();
    writeToCache("/test_file/file.txt");
    RevCommit theirs = commit(base);

    Git git = new Git(repo);
    git.branchCreate().setName("test_branch").setStartPoint(ours).call();
    git.checkout().setName("test_branch").call();

    CherryPickResult result = git.cherryPick().include(theirs).call();

    System.currentTimeMillis();
  }

}
