package com.beijunyi.parallelgit.web.workspace;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.beijunyi.parallelgit.web.data.RepositoryManager;

@Named
@Singleton
public class WorkspaceManager {

  private final Map<String, Workspace> workspaces = new ConcurrentHashMap<>();

  @Nonnull
  public Workspace prepareWorkspace() {
    String id = UUID.randomUUID().toString();
    Workspace ret = new Workspace(id, this);
    workspaces.put(id, ret);
    return ret;
  }

  public void destroyWorkspace(@Nonnull String id) throws IOException {
    Workspace workspace = workspaces.remove(id);
    if(workspace != null)
      workspace.destroy();
  }

}
