package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import com.beijunyi.parallelgit.filesystem.utils.GitParams;
import com.beijunyi.parallelgit.filesystem.utils.GitUriBuilder;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderNewFileSystemTest extends AbstractGitFileSystemTest {

  @Test
  public void openNonBareRepositoryFromUri() throws IOException {
    initFileRepository(false);
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try(GitFileSystem gfs = provider.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
      Repository repo = gfs.getRepository();
      Assert.assertFalse(repo.isBare());
      Assert.assertEquals(repoDir, gfs.getRepository().getWorkTree());
    }
  }

  @Test
  public void openBareRepositoryFromUri() throws IOException {
    initFileRepository(true);
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try(GitFileSystem gfs = provider.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
      Repository repo = gfs.getRepository();
      Assert.assertTrue(repo.isBare());
      Assert.assertEquals(repoDir, repo.getDirectory());
    }
  }

  @Test
  public void openWithBranch() throws IOException {
    initFileRepository(true);
    writeToCache("some_file");
    RevCommit commit = CommitUtils.getCommit(repo, commitToBranch("test_branch"));
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try(GitFileSystem gfs = provider.newFileSystem(uri, Collections.singletonMap(GitParams.BRANCH_KEY, "test_branch"))) {
      Assert.assertEquals("test_branch", gfs.getBranch());

      RevCommit baseCommit = gfs.getCommit();
      Assert.assertNotNull(baseCommit);
      Assert.assertEquals(commit, baseCommit);

      AnyObjectId baseTree = gfs.getTree();
      Assert.assertNotNull(baseTree);
      Assert.assertEquals(commit.getTree(), baseTree);
    }
  }

  @Test
  public void openWithRevision() throws IOException {
    initFileRepository(true);
    writeToCache("some_file");
    RevCommit commit = CommitUtils.getCommit(repo, commitToMaster());
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try(GitFileSystem gfs = provider.newFileSystem(uri, Collections.singletonMap(GitParams.REVISION_KEY, commit))) {
      Assert.assertNull(gfs.getBranch());

      RevCommit baseCommit = gfs.getCommit();
      Assert.assertNotNull(baseCommit);
      Assert.assertEquals(commit, baseCommit);

      AnyObjectId baseTree = gfs.getTree();
      Assert.assertNotNull(baseTree);
      Assert.assertEquals(commit.getTree(), baseTree);
    }
  }

  @Test
  public void openWithTree() throws IOException {
    initFileRepository(true);
    writeToCache("some_file");
    RevTree tree = CommitUtils.getCommit(repo, commitToMaster()).getTree();
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try (GitFileSystem gfs = provider.newFileSystem(uri, Collections.singletonMap(GitParams.TREE_KEY, tree))) {
      Assert.assertNull(gfs.getBranch());

      Assert.assertNull(gfs.getCommit());

      AnyObjectId baseTree = gfs.getTree();
      Assert.assertNotNull(baseTree);
      Assert.assertEquals(tree, baseTree);
    }
  }


}
