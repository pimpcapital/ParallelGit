package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitFileSystemGetTreeTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws Exception {
    initRepository();
  }

  @Test
  public void getTreeWhenFileSystemWasCreatedFromBranch_theTreeShouldEqualToTheHeadCommitTree() throws IOException {
    writeSomeFileToCache();
    RevCommit head = commitToBranch("test_branch");

    GitFileSystem gfs = GitFileSystemBuilder.prepare().repository(repo).branch("test_branch").build();
    assertEquals(head.getTree(), gfs.getTree());
  }

  @Test
  public void getTreeWhenFileSystemWasCreatedFromCommit_theTreeShouldEqualToTheCommitTree() throws IOException {
    writeSomeFileToCache();
    RevCommit commit = commit(null);

    GitFileSystem gfs = GitFileSystemBuilder.prepare().repository(repo).commit(commit).build();
    assertEquals(commit.getTree(), gfs.getTree());
  }

  @Test
  public void getTreeWhenFileSystemWasCreatedFromTree_theTreeShouldEqualToTheInputTree() throws IOException {
    writeSomeFileToCache();
    AnyObjectId tree = commit(null).getTree();

    GitFileSystem gfs = GitFileSystemBuilder.prepare().repository(repo).tree(tree).build();
    assertEquals(tree, gfs.getTree());
  }

}
