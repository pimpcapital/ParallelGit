package com.beijunyi.parallelgit.filesystem.merge;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

public class GfsMergerConflictingMergeTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initFileRepository(false);
  }

  @Test
  public void mergeFileWithConflictingMode_() throws Exception {
    byte[] data = "some text data".getBytes();
    writeToCache("/test_file.txt", data, FileMode.REGULAR_FILE);
    RevCommit base = commit(null);

    clearCache();
    writeToCache("/test_file.txt", data, FileMode.EXECUTABLE_FILE);
    RevCommit ours = commit(base);

    clearCache();
    writeToCache("/test_file.txt", data, FileMode.SYMLINK);
    RevCommit theirs = commit(base);


    Git git = new Git(repo);
    git.branchCreate().setName("test_branch").setStartPoint(ours).call();
    git.checkout().setName("test_branch").call();

    CherryPickResult result = git.cherryPick().include(theirs).call();

    System.currentTimeMillis();
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


    Git git = new Git(repo);
    git.branchCreate().setName("test_branch").setStartPoint(ours).call();
    git.checkout().setName("test_branch").call();

    CherryPickResult result = git.cherryPick().include(theirs).call();

    System.currentTimeMillis();
  }

}
