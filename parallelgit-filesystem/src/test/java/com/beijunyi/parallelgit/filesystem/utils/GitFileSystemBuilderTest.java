package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.CommitHelper;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileSystemBuilderTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initFileRepository(false);
  }

  @Test
  public void buildForUri() throws IOException {
    URI uri = GitUriBuilder.prepare()
                .repository(repo)
                .build();
    GitFileSystem gfs = GitFileSystemBuilder.forUri(uri, GitParams.emptyMap())
                          .build();
    Assert.assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildForPath() throws IOException {
    Path path = repoDir.toPath();
    GitFileSystem gfs = GitFileSystemBuilder.forPath(path, GitParams.emptyMap())
                          .build();
    Assert.assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepository() throws IOException {
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repo)
                          .build();
    Assert.assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryDirectory() throws IOException {
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repo.getDirectory())
                          .build();
    Assert.assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryDirectoryPath() throws IOException {
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repo.getDirectory().getAbsolutePath())
                          .build();
    Assert.assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryWorkTree() throws IOException {
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repo.getWorkTree())
                          .build();
    Assert.assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryWorkTreePath() throws IOException {
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repo.getWorkTree().getAbsolutePath())
                          .build();
    Assert.assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildWithBranch() throws IOException {
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repo)
                          .branch("test_branch")
                          .build();
    Assert.assertEquals("test_branch", gfs.getBranch());
  }

  @Test
  public void buildWithRevision() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repo)
                          .commit(commit)
                          .build();
    Assert.assertEquals(commit, gfs.getCommit());
  }

  @Test
  public void buildWithRevisionString() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repo)
                          .commit(commit.getName())
                          .build();
    Assert.assertEquals(commit, gfs.getCommit());
  }

  @Test
  public void buildWithTree() throws IOException {
    writeSomeFileToCache();
    AnyObjectId tree = CommitHelper.getCommit(repo, commitToMaster()).getTree();
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repo)
                          .tree(tree)
                          .build();
    Assert.assertEquals(tree, gfs.getTree());
  }

  @Test
  public void buildWithTreeString() throws IOException {
    writeSomeFileToCache();
    AnyObjectId tree = CommitHelper.getCommit(repo, commitToMaster()).getTree();
    GitFileSystem gfs = GitFileSystemBuilder.prepare()
                          .repository(repo)
                          .tree(tree.getName())
                          .build();
    Assert.assertEquals(tree, gfs.getTree());
  }
}
