package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.beijunyi.parallelgit.utils.CommitHelper;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderNewFileSystemTest extends AbstractGitFileSystemTest {

  @Test
  public void openNewFileSystemOfNonBareFromUri() throws IOException {
    initRepository(false, false);
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try(FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
      Assert.assertNotNull(fs);
      Assert.assertTrue(fs instanceof GitFileSystem);
      Repository repo = ((GitFileSystem)fs).getFileStore().getRepository();
      Assert.assertFalse(repo.isBare());
      Assert.assertEquals(repoDir, ((GitFileSystem)fs).getFileStore().getRepository().getWorkTree());
    }
  }

  @Test
  public void openNewFileSystemOfBareRepositoryFromUri() throws IOException {
    initRepository(false, true);
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    try(FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
      Assert.assertNotNull(fs);
      Assert.assertTrue(fs instanceof GitFileSystem);
      Repository repo = ((GitFileSystem)fs).getFileStore().getRepository();
      Assert.assertTrue(repo.isBare());
      Assert.assertEquals(repoDir, repo.getDirectory());
    }
  }

  @Test
  public void openNewFileSystemWithSpecifiedBranchFromUriTest() throws IOException {
    initRepository(false, true);
    writeFile("some_file");
    RevCommit commit = CommitHelper.getCommit(repo, commitToBranch("branch"));
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .branch("branch")
                .build();
    try(FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
      GitFileSystem gfs = (GitFileSystem) fs;
      GitFileStore store = gfs.getFileStore();
      Assert.assertEquals("refs/heads/branch", store.getBranch());

      RevCommit baseCommit = store.getBaseCommit();
      Assert.assertNotNull(baseCommit);
      Assert.assertEquals(commit, baseCommit);

      AnyObjectId baseTree = store.getBaseTree();
      Assert.assertNotNull(baseTree);
      Assert.assertEquals(commit.getTree(), baseTree);
    }
  }

  @Test
  public void openNewFileSystemWithSpecifiedRevisionFromUriTest() throws IOException {
    initRepository(false, true);
    writeFile("some_file");
    RevCommit commit = CommitHelper.getCommit(repo, commitToBranch("some_branch"));
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .revision(commit)
                .build();
    try(FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
      GitFileSystem gfs = (GitFileSystem) fs;
      GitFileStore store = gfs.getFileStore();
      Assert.assertNull(store.getBranch());

      RevCommit baseCommit = store.getBaseCommit();
      Assert.assertNotNull(baseCommit);
      Assert.assertEquals(commit, baseCommit);

      AnyObjectId baseTree = store.getBaseTree();
      Assert.assertNotNull(baseTree);
      Assert.assertEquals(commit.getTree(), baseTree);
    }
  }

  @Test
  public void openNewFileSystemWithEnvMapOverridingParamTest() throws IOException {
    initRepository(false, true);
    URI uri = GitUriUtils.createUri(repoDir, null, "session_1", null, null, null, null, null);
    Map<String, Object> envMap = new HashMap<>();
    envMap.put(GitFileSystemProvider.SESSION_KEY, "session_2");
    try(FileSystem fs = FileSystems.newFileSystem(uri, envMap)) {
      Assert.assertEquals("session_2", ((GitFileSystem)fs).getSessionId());
    }
  }



}
