package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

public abstract class AbstractGitFileSystemTest extends AbstractParallelGitTest {

  protected static final String TEST_USER_NAME = "test";
  protected static final String TEST_USER_EMAIL = "test@email.com";

  protected GitFileSystem gfs;
  protected GitPath root;

  protected void initGitFileSystemForBranch(@Nonnull String branch) throws IOException {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(GitFileSystems.newFileSystem(repo, branch));
  }

  protected void initGitFileSystemForRevision(@Nonnull ObjectId revision) throws IOException {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(GitFileSystems.newFileSystem(repo, null, revision));
  }

  protected void initGitFileSystemForTree(@Nonnull ObjectId tree) throws IOException {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(GitFileSystems.newFileSystem(repo, null, null, tree));
  }

  protected void initGitFileSystem() throws IOException {
    if(repo == null)
      initRepository();
    initGitFileSystemForBranch(Constants.MASTER);
  }

  protected void injectGitFileSystem(@Nonnull GitFileSystem gfs) {
    this.gfs = gfs;
    root = gfs.getRoot();
  }

  protected void loadCache() throws IOException {
    gfs.getFileStore().initializeCache();
  }



}
