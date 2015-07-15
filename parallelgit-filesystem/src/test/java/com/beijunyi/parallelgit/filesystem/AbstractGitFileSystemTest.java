package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

public abstract class AbstractGitFileSystemTest extends AbstractParallelGitTest {

  protected static final String TEST_USER_NAME = "test";
  protected static final String TEST_USER_EMAIL = "test@email.com";

  protected final GitFileSystemProvider provider = GitFileSystemProvider.getInstance();
  protected GitFileSystem gfs;
  protected GitPath root;

  protected void initGitFileSystemForBranch(@Nonnull String branch) throws IOException {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(GitFileSystemBuilder.prepare()
                            .repository(repo)
                            .branch(branch)
                            .build());
  }

  protected void initGitFileSystemForRevision(@Nonnull ObjectId revisionId) throws IOException {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(GitFileSystemBuilder.prepare()
                            .repository(repo)
                            .commit(revisionId)
                            .build());
  }

  protected void initGitFileSystemForTree(@Nonnull ObjectId treeId) throws IOException {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(GitFileSystemBuilder.prepare()
                            .repository(repo)
                            .tree(treeId)
                            .build());
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
    gfs.getFileStore().prepareCache();
  }



}
