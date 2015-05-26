package com.beijunyi.parallelgit.gfs;

import com.beijunyi.parallelgit.utils.CommitHelper;
import com.beijunyi.parallelgit.utils.RefHelper;
import com.beijunyi.parallelgit.utils.RevTreeHelper;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreNameTest extends AbstractGitFileSystemTest {

  @Test
  public void nameOfGitFileStoreCreatedWithOnlyBranchSpecifiedTest() {
    initRepository();
    writeFile("a.txt");
    String branch = "test_branch";
    ObjectId commit = commitToBranch(branch);
    initGitFileSystemForBranch(branch);

    String expectedName = repoDir.getAbsolutePath()
                            + ":" + RefHelper.getBranchRefName(branch)
                            + ":" + commit.getName()
                            + ":" + CommitHelper.getCommit(repo, commit).getTree().getName();
    Assert.assertEquals(expectedName, gfs.getFileStore().name());
  }

  @Test
  public void nameOfGitFileStoreCreatedWithOnlyRevisionSpecifiedTest() {
    initRepository();
    writeFile("a.txt");
    ObjectId commit = commitToMaster();
    initGitFileSystemForRevision(commit);

    String expectedName = repoDir.getAbsolutePath()
                            + ":"
                            + ":" + commit.getName()
                            + ":" + CommitHelper.getCommit(repo, commit).getTree().getName();
    Assert.assertEquals(expectedName, gfs.getFileStore().name());
  }

  @Test
  public void nameOfGitFileStoreCreatedWithOnlyTreeSpecifiedTest() {
    initRepository();
    writeFile("a.txt");
    ObjectId tree = RevTreeHelper.getTree(repo, commitToMaster());
    initGitFileSystemForTree(tree);

    String expectedName = repoDir.getAbsolutePath()
                            + ":"
                            + ":"
                            + ":" + tree.getName();
    Assert.assertEquals(expectedName, gfs.getFileStore().name());
  }

  @Test
  public void nameOfGitFileStoreCreatedWithRevisionOverriddenTest() {
    initRepository();
    writeFile("a.txt");
    String branch = "test_branch";
    commitToBranch(branch);

    clearCache();
    writeFile("b.txt");
    ObjectId commit = commit("test commit", null);

    injectGitFileSystem(GitFileSystems.newFileSystem(repo, branch, commit));

    String expectedName = repoDir.getAbsolutePath()
                            + ":" + RefHelper.getBranchRefName(branch)
                            + ":" + commit.getName()
                            + ":" + CommitHelper.getCommit(repo, commit).getTree().getName();
    Assert.assertEquals(expectedName, gfs.getFileStore().name());
  }

  @Test
  public void nameOfGitFileStoreCreatedWithTreeOverriddenTest() {
    initRepository();
    writeFile("a.txt");
    String branch = "test_branch";
    ObjectId commit = commitToBranch(branch);

    clearCache();
    writeFile("b.txt");
    ObjectId tree = RevTreeHelper.getTree(repo, commit("test commit", null));

    injectGitFileSystem(GitFileSystems.newFileSystem(repo, branch, null, tree));

    String expectedName = repoDir.getAbsolutePath()
                            + ":" + RefHelper.getBranchRefName(branch)
                            + ":" + commit.getName()
                            + ":" + tree.getName();
    Assert.assertEquals(expectedName, gfs.getFileStore().name());
  }

  @Test
  public void nameOfGitFileStoreCreatedWithRevisionAndTreeOverriddenTest() {
    initRepository();
    writeFile("a.txt");
    String branch = "test_branch";
    commitToBranch(branch);

    clearCache();
    writeFile("b.txt");
    ObjectId commit = commit("test commit 1", null);

    clearCache();
    writeFile("c.txt");
    ObjectId tree = RevTreeHelper.getTree(repo, commit("test commit 2", null));

    injectGitFileSystem(GitFileSystems.newFileSystem(repo, branch, commit, tree));

    String expectedName = repoDir.getAbsolutePath()
                            + ":" + RefHelper.getBranchRefName(branch)
                            + ":" + commit.getName()
                            + ":" + tree.getName();
    Assert.assertEquals(expectedName, gfs.getFileStore().name());
  }
}
