package com.beijunyi.parallelgit.filesystem.requests;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.*;

@Ignore
public class MergeRequestTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initFileRepository(false);
  }

  @Test
  public void mergeAutoMergeableChanges_() throws Exception {
    writeToCache("/test_file.txt", "a\nb\nc\nd\ne");
    RevCommit base = commit(null);

    clearCache();
    writeToCache("/test_file.txt", "a\nB\nc\nd\ne");
    RevCommit ours = commit(base);

    clearCache();
    writeToCache("/test_file.txt", "a\nB\nc\nD\nd\ne");
    RevCommit theirs = commit(base);

    Git git = new Git(repo);
    git.branchCreate().setName("test_branch").setStartPoint(ours).call();
    git.checkout().setName("test_branch").call();
    MergeResult result = git.merge().include(theirs).call();
    Assert.assertTrue(result.getNewHead() != null);
  }
}
