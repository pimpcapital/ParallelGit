package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.NoRepositoryException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitFileSystemBuilderTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initFileRepository(false);
  }

  @Test
  public void buildFromRepository() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repo)
                          .build();
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryDirectory() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repo.getDirectory())
                          .build();
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryDirectoryPath() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repo.getDirectory().getAbsolutePath())
                          .build();
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryWorkTree() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repo.getWorkTree())
                          .build();
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryWorkTreePath() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repo.getWorkTree().getAbsolutePath())
                          .build();
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildWithBranch() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repo)
                          .branch("test_branch")
                          .build();
    assertEquals("test_branch", gfs.getBranch());
  }

  @Test
  public void buildWithRevision() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repo)
                          .commit(commit)
                          .build();
    assertEquals(commit, gfs.getCommit());
  }

  @Test
  public void buildWithRevisionString() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repo)
                          .commit(commit.getName())
                          .build();
    assertEquals(commit, gfs.getCommit());
  }

  @Test
  public void buildWithTree() throws IOException {
    writeSomeFileToCache();
    AnyObjectId tree = commitToMaster().getTree();
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repo)
                          .tree(tree)
                          .build();
    assertEquals(tree, gfs.getTree());
  }

  @Test
  public void buildWithTreeString() throws IOException {
    writeSomeFileToCache();
    AnyObjectId tree = commitToMaster().getTree();
    GitFileSystem gfs = Gfs.newFileSystem()
                          .repository(repo)
                          .tree(tree.getName())
                          .build();
    assertEquals(tree, gfs.getTree());
  }

  @Test(expected = NoRepositoryException.class)
  public void buildWithoutRepository_shouldThrowNoRepositoryException() throws IOException {
    Gfs.newFileSystem().build();
  }
}
