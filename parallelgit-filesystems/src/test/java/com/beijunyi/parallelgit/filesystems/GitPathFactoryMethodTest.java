package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;
import java.net.URI;

import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class GitPathFactoryMethodTest extends AbstractGitFileSystemTest {

  @Test
  public void createGitPathFromRepoTest() throws IOException {
    initRepository();
    GitPath path = GitPaths.get(repo, "/some_path");
    Assert.assertEquals("/some_path", path.toString());
    Assert.assertEquals(repo, path.getFileSystem().getFileStore().getRepository());
  }

  @Test
  public void createGitPathFromRepoAndRevisionTest() throws IOException {
    initRepository();
    writeFile("some_dir/some_child_dir/some_file.txt");
    ObjectId revision = commitToMaster();
    GitPath path = GitPaths.get(repo, revision.getName(), "some_dir", "some_child_dir", "some_file.txt");
    Assert.assertEquals("/some_dir/some_child_dir/some_file.txt", path.toString());
    Assert.assertEquals(revision, path.getFileSystem().getFileStore().getBaseCommit());
    Assert.assertEquals(repo, path.getFileSystem().getFileStore().getRepository());
  }

  @Test
  public void createGitPathFromUriTest() throws IOException {
    initRepository(false, false);
    URI uri = GitUriUtils.createUri(repoDir, "/some_path", null);
    GitPath path = GitPaths.get(uri);
    Assert.assertEquals("/some_path", path.toString());
    Assert.assertEquals(repoDir, path.getFileSystem().getFileStore().getRepository().getWorkTree());
  }
}
