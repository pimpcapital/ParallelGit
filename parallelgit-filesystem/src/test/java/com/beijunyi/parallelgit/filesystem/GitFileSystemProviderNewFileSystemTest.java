package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import com.beijunyi.parallelgit.filesystem.utils.GitParams;
import com.beijunyi.parallelgit.filesystem.utils.GitUriBuilder;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitFileSystemProviderNewFileSystemTest extends AbstractGitFileSystemTest {

  @Test
  public void openNonBareRepositoryFromUri() throws IOException {
    initFileRepository(false);
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try(GitFileSystem gfs = provider.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
      Repository repo = gfs.getRepository();
      assertFalse(repo.isBare());
      assertEquals(repoDir, gfs.getRepository().getWorkTree());
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
      assertTrue(repo.isBare());
      assertEquals(repoDir, repo.getDirectory());
    }
  }

  @Test
  public void openWithBranch() throws IOException {
    initFileRepository(true);
    writeToCache("some_file");
    RevCommit commit = commitToBranch("test_branch");
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try(GitFileSystem gfs = provider.newFileSystem(uri, Collections.singletonMap(GitParams.BRANCH_KEY, "test_branch"))) {
      assertEquals("test_branch", gfs.getBranch());

      RevCommit baseCommit = gfs.getCommit();
      assertNotNull(baseCommit);
      assertEquals(commit, baseCommit);

      AnyObjectId baseTree = gfs.getTree();
      assertNotNull(baseTree);
      assertEquals(commit.getTree(), baseTree);
    }
  }

  @Test
  public void openWithRevision() throws IOException {
    initFileRepository(true);
    writeToCache("some_file");
    RevCommit commit = commitToMaster();
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try(GitFileSystem gfs = provider.newFileSystem(uri, Collections.singletonMap(GitParams.REVISION_KEY, commit))) {
      assertNull(gfs.getBranch());

      RevCommit baseCommit = gfs.getCommit();
      assertNotNull(baseCommit);
      assertEquals(commit, baseCommit);

      AnyObjectId baseTree = gfs.getTree();
      assertNotNull(baseTree);
      assertEquals(commit.getTree(), baseTree);
    }
  }

  @Test
  public void openWithTree() throws IOException {
    initFileRepository(true);
    writeToCache("some_file");
    RevTree tree = commitToMaster().getTree();
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try (GitFileSystem gfs = provider.newFileSystem(uri, Collections.singletonMap(GitParams.TREE_KEY, tree))) {
      assertNull(gfs.getBranch());

      assertNull(gfs.getCommit());

      AnyObjectId baseTree = gfs.getTree();
      assertNotNull(baseTree);
      assertEquals(tree, baseTree);
    }
  }


}
