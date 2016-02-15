package com.beijunyi.parallelgit.web.workspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.web.workspace.status.Head;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

public class Workspace {

  private final String id;
  private final WorkspaceManager workspaceManager;

  private User user;
  private Repository repo;
  private GitFileSystem gfs;

  public Workspace(@Nonnull String id, @Nonnull WorkspaceManager workspaceManager) {
    this.id = id;
    this.workspaceManager = workspaceManager;
  }

  public void init(@Nonnull User user, @Nonnull Repository repo) throws IOException {
    this.user = user;
    this.repo = repo;
    gfs = Gfs.newFileSystem(getDefaultBranch(repo), repo);
  }

  @Nonnull
  public WorkspaceStatus getStatus() {
    return new WorkspaceStatus(Head.of(gfs));
  }

  @Nonnull
  public Repository getRepo() {
    if(repo == null)
      throw new IllegalStateException();
    return repo;
  }

  public void setRepo(@Nonnull Repository repo) {
    if(this.repo != null)
      throw new IllegalStateException();
    this.repo = repo;
  }

  public void setUser(@Nonnull User user) {
    this.user = user;
  }

  @Nonnull
  public GitFileSystem getFileSystem() {
    if(gfs == null)
      throw new IllegalStateException();
    return gfs;
  }

  public void destroy() throws IOException {
    if(gfs != null)
      gfs.close();
    workspaceManager.destroyWorkspace(id);
  }

  @Nonnull
  private static String getDefaultBranch(@Nonnull Repository repo) throws IOException {
    if(BranchUtils.branchExists(Constants.MASTER, repo))
      return Constants.MASTER;
    List<String> branches = new ArrayList<>(BranchUtils.getBranches(repo).keySet());
    if(branches.isEmpty())
      return Constants.MASTER;
    Collections.sort(branches);
    return branches.get(0);
  }

}
