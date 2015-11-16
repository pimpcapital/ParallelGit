package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsForRevisionTest extends AbstractParallelGitTest {

  @Test
  public void buildFileSystemForBranch_theResultFileSystemBranchShouldBeTheInputBranch() throws IOException {
    initFileRepository(true);
    writeSomeFileToCache();
    commitToBranch("test_branch");
    GitFileSystem gfs = Gfs.forRevision("test_branch", repo);
    assertEquals("test_branch", gfs.getBranch());
  }

  @Test
  public void buildFileSystemForCommit_theResultFileSystemCommitShouldBeTheInputCommit() throws IOException {
    initFileRepository(true);
    writeSomeFileToCache();
    RevCommit commit = commit(null);
    GitFileSystem gfs = Gfs.forRevision(commit.getName(), repo);
    assertEquals(commit, gfs.getCommit());
  }

  @Test
  public void buildFileSystemFromBareRepositoryDirectory_theResultFileSystemRepositoryDirectoryShouldEqualTheInputDirectory() throws IOException {
    initFileRepository(true);
    writeSomeFileToCache();
    commitToBranch("test_branch");
    GitFileSystem gfs = Gfs.forRevision("test_branch", repoDir);
    assertEquals(repoDir, gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFileSystemFromNonBareRepositoryDirectory_theResultFileSystemRepositoryDirectoryShouldEqualTheInputDirectory() throws IOException {
    initFileRepository(false);
    writeSomeFileToCache();
    commitToBranch("test_branch");
    GitFileSystem gfs = Gfs.forRevision("test_branch", repoDir);
    assertEquals(repoDir, gfs.getRepository().getWorkTree());
  }

  @Test
  public void buildFileSystemFromBareRepositoryDirectoryString_theResultFileSystemRepositoryDirectoryShouldEqualTheInputDirectory() throws IOException {
    initFileRepository(true);
    writeSomeFileToCache();
    commitToBranch("test_branch");
    GitFileSystem gfs = Gfs.forRevision("test_branch", repoDir.toString());
    assertEquals(repoDir, gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFileSystemFromNonBareRepositoryDirectoryString_theResultFileSystemRepositoryDirectoryShouldEqualTheInputDirectory() throws IOException {
    initFileRepository(false);
    writeSomeFileToCache();
    commitToBranch("test_branch");
    GitFileSystem gfs = Gfs.forRevision("test_branch", repoDir.toString());
    assertEquals(repoDir, gfs.getRepository().getWorkTree());
  }


}
