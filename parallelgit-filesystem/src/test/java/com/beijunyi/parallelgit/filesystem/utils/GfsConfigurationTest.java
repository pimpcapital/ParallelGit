package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsConfigurationTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initFileRepository(false);
  }

  @Test
  public void buildFromRepository() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo);
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryDirectory() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo.getDirectory());
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryDirectoryPath() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo.getDirectory().getAbsolutePath());
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryWorkTree() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo.getWorkTree());
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryWorkTreePath() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo.getWorkTree().getAbsolutePath());
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildWithBranch() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem("test_branch", repo);
    assertEquals("test_branch", gfs.getStatusProvider().branch());
  }

  @Test
  public void buildWithRevision() throws IOException {
    writeSomethingToCache();
    AnyObjectId commit = commitToMaster();
    GitFileSystem gfs = Gfs.newFileSystem(commit, repo);
    assertEquals(commit, gfs.getStatusProvider().commit());
  }

  @Test
  public void buildWithRevisionString() throws IOException {
    writeSomethingToCache();
    AnyObjectId commit = commitToMaster();
    GitFileSystem gfs = Gfs.newFileSystem(commit.getName(), repo);
    assertEquals(commit, gfs.getStatusProvider().commit());
  }

}
