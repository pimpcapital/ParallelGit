package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import com.beijunyi.parallelgit.utils.CommitHelper;
import com.beijunyi.parallelgit.utils.RefHelper;
import com.beijunyi.parallelgit.utils.RevTreeHelper;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class GitFileStoreNameTest extends AbstractGitFileSystemTest {

  @Test
  public void nameOfGitFileStoreCreatedWithOnlyBranchSpecifiedTest() throws IOException {
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
  public void nameOfGitFileStoreCreatedWithOnlyRevisionSpecifiedTest() throws IOException {
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
  public void nameOfGitFileStoreCreatedWithOnlyTreeSpecifiedTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    ObjectId tree = RevTreeHelper.getRootTree(repo, commitToMaster());
    initGitFileSystemForTree(tree);

    String expectedName = repoDir.getAbsolutePath()
                            + ":"
                            + ":"
                            + ":" + tree.getName();
    Assert.assertEquals(expectedName, gfs.getFileStore().name());
  }

  @Test
  public void nameOfGitFileStoreCreatedWithRevisionOverriddenTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    String branch = "test_branch";
    commitToBranch(branch);

    clearCache();
    writeFile("b.txt");
    ObjectId commit = commit("test commit", null);

    injectGitFileSystem(GitFileSystemBuilder.prepare()
                          .repository(repo)
                          .branch(branch)
                          .commit(commit)
                          .build());

    String expectedName = repoDir.getAbsolutePath()
                            + ":" + RefHelper.getBranchRefName(branch)
                            + ":" + commit.getName()
                            + ":" + CommitHelper.getCommit(repo, commit).getTree().getName();
    Assert.assertEquals(expectedName, gfs.getFileStore().name());
  }

  @Test
  public void nameOfGitFileStoreCreatedWithTreeOverriddenTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    String branch = "test_branch";
    ObjectId commit = commitToBranch(branch);

    clearCache();
    writeFile("b.txt");
    ObjectId tree = RevTreeHelper.getRootTree(repo, commit("test commit", null));

    injectGitFileSystem(GitFileSystemBuilder.prepare()
                          .repository(repo)
                          .branch(branch)
                          .tree(tree)
                          .build());

    String expectedName = repoDir.getAbsolutePath()
                            + ":" + RefHelper.getBranchRefName(branch)
                            + ":" + commit.getName()
                            + ":" + tree.getName();
    Assert.assertEquals(expectedName, gfs.getFileStore().name());
  }

  @Test
  public void nameOfGitFileStoreCreatedWithRevisionAndTreeOverriddenTest() throws IOException {
    initRepository();
    writeFile("a.txt");
    String branch = "test_branch";
    commitToBranch(branch);

    clearCache();
    writeFile("b.txt");
    ObjectId commit = commit("test commit 1", null);

    clearCache();
    writeFile("c.txt");
    ObjectId tree = RevTreeHelper.getRootTree(repo, commit("test commit 2", null));

    injectGitFileSystem(GitFileSystemBuilder.prepare()
                          .repository(repo)
                          .branch(branch)
                          .commit(commit)
                          .tree(tree)
                          .build());

    String expectedName = repoDir.getAbsolutePath()
                            + ":" + RefHelper.getBranchRefName(branch)
                            + ":" + commit.getName()
                            + ":" + tree.getName();
    Assert.assertEquals(expectedName, gfs.getFileStore().name());
  }
}
