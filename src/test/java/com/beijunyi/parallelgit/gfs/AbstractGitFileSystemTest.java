package com.beijunyi.parallelgit.gfs;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.After;

public abstract class AbstractGitFileSystemTest extends AbstractParallelGitTest {

  protected static final String TEST_USER_NAME = "test";
  protected static final String TEST_USER_EMAIL = "test@email.com";

  private boolean keepFileSystem;
  protected GitFileSystem gfs;
  protected GitPath root;

  @After
  public void destroyGitFileSystem() {
    if(!keepFileSystem && gfs != null && gfs.isOpen()) {
      gfs.close();
      gfs = null;
      root = null;
    }
  }

  protected void initGitFileSystemForBranch(@Nonnull String branch) {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(GitFileSystems.newFileSystem(repo, branch));
  }

  protected void initGitFileSystemForRevision(@Nonnull ObjectId revision) {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(GitFileSystems.newFileSystem(repo, null, revision));
  }

  protected void initGitFileSystemForTree(@Nonnull ObjectId tree) {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(GitFileSystems.newFileSystem(repo, null, null, tree));
  }

  protected void initGitFileSystem() {
    if(repo == null)
      initRepository();
    initGitFileSystemForBranch(Constants.MASTER);
  }

  protected void injectGitFileSystem(@Nonnull GitFileSystem gfs) {
    this.gfs = gfs;
    root = gfs.getRoot();
  }

  protected void preventDestroyFileSystem() {
    preventDestroyRepo();
    keepFileSystem = true;
  }

  protected void loadCache() {
    gfs.getFileStore().initializeCache();
  }



}
