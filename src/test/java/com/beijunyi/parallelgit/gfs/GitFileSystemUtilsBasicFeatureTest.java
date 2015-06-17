package com.beijunyi.parallelgit.gfs;

import java.io.IOException;

import com.beijunyi.parallelgit.util.RevTreeHelper;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemUtilsBasicFeatureTest extends AbstractGitFileSystemTest {

  @Test
  public void getRepositoryFromFileStore() throws IOException {
    initGitFileSystem();
    Assert.assertEquals(repo, GitFileSystemUtils.getRepository(gfs.getFileStore()));
  }

  @Test
  public void getRepositoryFromFileSystem() throws IOException {
    initGitFileSystem();
    Assert.assertEquals(repo, GitFileSystemUtils.getRepository(gfs));
  }

  @Test
  public void getRepositoryFromPath() throws IOException {
    initGitFileSystem();
    Assert.assertEquals(repo, GitFileSystemUtils.getRepository(root));
  }

  @Test
  public void getBranchFromFileStore() throws IOException {
    initGitFileSystem();
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "some_branch"));
    Assert.assertEquals("refs/heads/some_branch", GitFileSystemUtils.getBranch(gfs.getFileStore()));
  }

  @Test
  public void getBranchFromFileSystem() throws IOException {
    initGitFileSystem();
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "some_branch"));
    Assert.assertEquals("refs/heads/some_branch", GitFileSystemUtils.getBranch(gfs));
  }

  @Test
  public void getBranchFromPath() throws IOException {
    initRepository();
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "some_branch"));
    Assert.assertEquals("refs/heads/some_branch", GitFileSystemUtils.getBranch(root));
  }

  @Test
  public void getBaseCommitFromFileStore() throws IOException {
    initRepository();
    writeFile("file.txt");
    ObjectId commit = commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(commit, GitFileSystemUtils.getBaseCommit(gfs.getFileStore()));
  }

  @Test
  public void getBaseCommitFromFileSystem() throws IOException {
    initRepository();
    writeFile("file.txt");
    ObjectId commit = commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(commit, GitFileSystemUtils.getBaseCommit(gfs));
  }

  @Test
  public void getBaseCommitFromPath() throws IOException {
    initRepository();
    writeFile("file.txt");
    ObjectId commit = commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(commit, GitFileSystemUtils.getBaseCommit(root));
  }

  @Test
  public void getBaseTreeFromFileStore() throws IOException {
    initRepository();
    writeFile("file.txt");
    RevTree tree = RevTreeHelper.getRootTree(repo, commitToMaster());
    initGitFileSystem();
    Assert.assertEquals(tree, GitFileSystemUtils.getBaseTree(gfs.getFileStore()));
  }

  @Test
  public void getBaseTreeFromFileSystem() throws IOException {
    initRepository();
    writeFile("file.txt");
    RevTree tree = RevTreeHelper.getRootTree(repo, commitToMaster());
    initGitFileSystem();
    Assert.assertEquals(tree, GitFileSystemUtils.getBaseTree(gfs));
  }

  @Test
  public void getBaseTreeFromPath() throws IOException {
    initRepository();
    writeFile("file.txt");
    RevTree tree = RevTreeHelper.getRootTree(repo, commitToMaster());
    initGitFileSystem();
    Assert.assertEquals(tree, GitFileSystemUtils.getBaseTree(root));
  }

}
