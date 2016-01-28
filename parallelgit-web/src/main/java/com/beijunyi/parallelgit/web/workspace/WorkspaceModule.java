package com.beijunyi.parallelgit.web.workspace;

import com.google.inject.AbstractModule;

public class WorkspaceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(WorkspaceManager.class).toInstance(new WorkspaceManager());
  }

}
