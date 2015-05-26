package com.beijunyi.parallelgit.gfs;

import com.beijunyi.parallelgit.utils.RevTreeHelper;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.junit.Assert;
import org.junit.Test;

public class GitFileSystemUtilsBasicFeatureTest extends AbstractGitFileSystemTest {

  @Test
  public void getRepositoryFromFileStore() {
    initGitFileSystem();
    Assert.assertEquals(repo, GitFileSystemUtils.getRepository(gfs.getFileStore()));
  }

  @Test
  public void getRepositoryFromFileSystem() {
    initGitFileSystem();
    Assert.assertEquals(repo, GitFileSystemUtils.getRepository(gfs));
  }

  @Test
  public void getRepositoryFromPath() {
    initGitFileSystem();
    Assert.assertEquals(repo, GitFileSystemUtils.getRepository(root));
  }

  @Test
  public void getBranchFromFileStore() {
    initGitFileSystem();
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "some_branch"));
    Assert.assertEquals("refs/heads/some_branch", GitFileSystemUtils.getBranch(gfs.getFileStore()));
  }

  @Test
  public void getBranchFromFileSystem() {
    initGitFileSystem();
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "some_branch"));
    Assert.assertEquals("refs/heads/some_branch", GitFileSystemUtils.getBranch(gfs));
  }

  @Test
  public void getBranchFromPath() {
    initRepository();
    injectGitFileSystem(GitFileSystems.newFileSystem(repo, "some_branch"));
    Assert.assertEquals("refs/heads/some_branch", GitFileSystemUtils.getBranch(root));
  }

  @Test
  public void getBaseCommitFromFileStore() {
    initRepository();
    writeFile("file.txt");
    ObjectId commit = commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(commit, GitFileSystemUtils.getBaseCommit(gfs.getFileStore()));
  }

  @Test
  public void getBaseCommitFromFileSystem() {
    initRepository();
    writeFile("file.txt");
    ObjectId commit = commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(commit, GitFileSystemUtils.getBaseCommit(gfs));
  }

  @Test
  public void getBaseCommitFromPath() {
    initRepository();
    writeFile("file.txt");
    ObjectId commit = commitToMaster();
    initGitFileSystem();
    Assert.assertEquals(commit, GitFileSystemUtils.getBaseCommit(root));
  }

  @Test
  public void getBaseTreeFromFileStore() {
    initRepository();
    writeFile("file.txt");
    RevTree tree = RevTreeHelper.getTree(repo, commitToMaster());
    initGitFileSystem();
    Assert.assertEquals(tree, GitFileSystemUtils.getBaseTree(gfs.getFileStore()));
  }

  @Test
  public void getBaseTreeFromFileSystem() {
    initRepository();
    writeFile("file.txt");
    RevTree tree = RevTreeHelper.getTree(repo, commitToMaster());
    initGitFileSystem();
    Assert.assertEquals(tree, GitFileSystemUtils.getBaseTree(gfs));
  }

  @Test
  public void getBaseTreeFromPath() {
    initRepository();
    writeFile("file.txt");
    RevTree tree = RevTreeHelper.getTree(repo, commitToMaster());
    initGitFileSystem();
    Assert.assertEquals(tree, GitFileSystemUtils.getBaseTree(root));
  }

}
