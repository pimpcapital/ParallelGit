package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import com.beijunyi.parallelgit.utils.CommitHelper;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemProviderNewFileSystemTest extends AbstractGitFileSystemTest {

  @Test
  public void openNewFileSystemOfNonBareFromUriTest() throws IOException {
    initRepository(false);
    URI uri = GitUriUtils.createUri(repoDir, null, null);
    try(FileSystem fs = FileSystems.newFileSystem(uri, null)) {
      Assert.assertNotNull(fs);
      Assert.assertTrue(fs instanceof GitFileSystem);
      Repository repo = ((GitFileSystem)fs).getFileStore().getRepository();
      Assert.assertFalse(repo.isBare());
      Assert.assertEquals(repoDir, ((GitFileSystem)fs).getFileStore().getRepository().getWorkTree());
    }
  }

  @Test
  public void openNewFileSystemOfBareRepositoryFromUriTest() throws IOException {
    initRepository();
    URI uri = GitUriUtils.createUri(repoDir, null, null, true, null, null, null, null);
    try(FileSystem fs = FileSystems.newFileSystem(uri, null)) {
      Assert.assertNotNull(fs);
      Assert.assertTrue(fs instanceof GitFileSystem);
      Repository repo = ((GitFileSystem)fs).getFileStore().getRepository();
      Assert.assertTrue(repo.isBare());
      Assert.assertEquals(repoDir, repo.getDirectory());
    }
  }

  @Test
  public void openNewFileSystemFromUriWithFileInRepoTest() throws IOException {
    initRepository();
    URI uri = GitUriUtils.createUri(repoDir, "some_path", null, true, null, null, null, null);
    try(FileSystem fs = FileSystems.newFileSystem(uri, null)) {
      Assert.assertEquals(repoDir, ((GitFileSystem)fs).getFileStore().getRepository().getDirectory());
    }
  }

  @Test
  public void openNewFileSystemFromUriWithSessionIdTest() throws IOException {
    initRepository();
    URI uri = GitUriUtils.createUri(repoDir, null, "session_id", true, null, null, null, null);
    try(FileSystem fs = FileSystems.newFileSystem(uri, null)) {
      Assert.assertEquals("session_id", ((GitFileSystem)fs).getSessionId());
    }
  }

  @Test
  public void openNewFileSystemWithCreatingNonBareRepositoryFromUriTest() throws IOException {
    initRepositoryDir();
    URI uri = GitUriUtils.createUri(repoDir, null, null, false, true, null, null, null);
    try(FileSystem fs = FileSystems.newFileSystem(uri, null)) {
      Repository repo = ((GitFileSystem)fs).getFileStore().getRepository();
      Assert.assertFalse(repo.isBare());
      Assert.assertEquals(repoDir, repo.getWorkTree());
    }
  }

  @Test
  public void openNewFileSystemWithCreatingBareRepositoryFromUriTest() throws IOException {
    initRepositoryDir();
    URI uri = GitUriUtils.createUri(repoDir, null, null, true, true, null, null, null);
    try(FileSystem fs = FileSystems.newFileSystem(uri, null)) {
      Repository repo = ((GitFileSystem)fs).getFileStore().getRepository();
      Assert.assertTrue(repo.isBare());
      Assert.assertEquals(repoDir, repo.getDirectory());
    }
  }

  @Test
  public void openNewFileSystemWithSpecifiedBranchFromUriTest() throws IOException {
    initRepository();
    writeFile("some_file");
    RevCommit commit = CommitHelper.getCommit(repo, commitToBranch("branch"));
    URI uri = GitUriUtils.createUri(repoDir, null, null, true, null, "branch", null, null);
    try(FileSystem fs = FileSystems.newFileSystem(uri, null)) {
      GitFileSystem gfs = (GitFileSystem) fs;
      GitFileStore store = gfs.getFileStore();
      Assert.assertEquals("refs/heads/branch", store.getBranch());

      RevCommit baseCommit = store.getBaseCommit();
      Assert.assertNotNull(baseCommit);
      Assert.assertEquals(commit, baseCommit);

      ObjectId baseTree = store.getBaseTree();
      Assert.assertNotNull(baseTree);
      Assert.assertEquals(commit.getTree(), baseTree);
    }
  }

  @Test
  public void openNewFileSystemWithSpecifiedRevisionFromUriTest() throws IOException {
    initRepository();
    writeFile("some_file");
    RevCommit commit = CommitHelper.getCommit(repo, commitToBranch("some_branch"));
    URI uri = GitUriUtils.createUri(repoDir, null, null, true, null, null, commit.getName(), null);
    try(FileSystem fs = FileSystems.newFileSystem(uri, null)) {
      GitFileSystem gfs = (GitFileSystem) fs;
      GitFileStore store = gfs.getFileStore();
      Assert.assertNull(store.getBranch());

      RevCommit baseCommit = store.getBaseCommit();
      Assert.assertNotNull(baseCommit);
      Assert.assertEquals(commit, baseCommit);

      ObjectId baseTree = store.getBaseTree();
      Assert.assertNotNull(baseTree);
      Assert.assertEquals(commit.getTree(), baseTree);
    }
  }

  @Test
  public void openNewFileSystemWithEnvMapOverridingParamTest() throws IOException {
    initRepository();
    URI uri = GitUriUtils.createUri(repoDir, null, "session_1", null, null, null, null, null);
    Map<String, Object> envMap = new HashMap<>();
    envMap.put(GitFileSystemProvider.SESSION_KEY, "session_2");
    try(FileSystem fs = FileSystems.newFileSystem(uri, envMap)) {
      Assert.assertEquals("session_2", ((GitFileSystem)fs).getSessionId());
    }
  }



}
