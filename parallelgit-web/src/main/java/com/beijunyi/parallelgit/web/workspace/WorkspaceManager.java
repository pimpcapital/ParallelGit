package com.beijunyi.parallelgit.web.workspace;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.beijunyi.parallelgit.web.data.RepositoryManager;

@Named
@Singleton
public class WorkspaceManager {

  private final RepositoryManager repositories;
  private final Map<String, Workspace> workspaces = new ConcurrentHashMap<>();

  @Inject
  public WorkspaceManager(@Nonnull RepositoryManager repositories) {
    this.repositories = repositories;
  }

  @Nonnull
  public Workspace prepareWorkspace(@Nonnull String id, @Nonnull GitUser user) {
    synchronized(workspaces) {
      if(workspaces.containsKey(id))
        throw new IllegalStateException();

      Workspace ret = new Workspace(id, user, repositories.getRepository());
      workspaces.put(id, ret);
      return ret;
    }
  }

  public void destroyWorkspace(@Nonnull String id) {
    synchronized(workspaces) {
      workspaces.remove(id);
    }
  }

}
