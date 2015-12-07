package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsBuilderTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initFileRepository(false);
  }

  @Test
  public void buildFromRepository() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo)
                          .build();
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryDirectory() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo.getDirectory())
                          .build();
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryDirectoryPath() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo.getDirectory().getAbsolutePath())
                          .build();
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryWorkTree() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo.getWorkTree())
                          .build();
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromRepositoryWorkTreePath() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo.getWorkTree().getAbsolutePath())
                          .build();
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildWithBranch() throws IOException {
    GitFileSystem gfs = Gfs.newFileSystem(repo)
                          .branch("test_branch")
                          .build();
    assertEquals("test_branch", gfs.getStatusProvider().branch());
  }

  @Test
  public void buildWithRevision() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    GitFileSystem gfs = Gfs.newFileSystem(repo)
                          .commit(commit)
                          .build();
    assertEquals(commit, gfs.getStatusProvider().commit());
  }

  @Test
  public void buildWithRevisionString() throws IOException {
    writeSomeFileToCache();
    AnyObjectId commit = commitToMaster();
    GitFileSystem gfs = Gfs.newFileSystem(repo)
                          .commit(commit.getName())
                          .build();
    assertEquals(commit, gfs.getStatusProvider().commit());
  }

}
