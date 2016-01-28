package com.beijunyi.parallelgit.web.config;

import javax.annotation.Nonnull;
import javax.servlet.annotation.WebListener;

import com.beijunyi.parallelgit.web.connection.ConnectionModule;
import com.beijunyi.parallelgit.web.workspace.WorkspaceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

@WebListener
public class InjectorFactory extends GuiceServletContextListener {

  @Nonnull
  public static Injector getInstance() {
    return GuiceHolder.INSTANCE;
  }

  @Override
  protected Injector getInjector() {
    return getInstance();
  }

  private static class GuiceHolder {
    private static final Injector INSTANCE = Guice.createInjector(new ConnectionModule(), new WorkspaceModule());
  }

}
