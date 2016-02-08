package com.beijunyi.parallelgit.web.protocol;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ProtocolModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<RequestHandler> binder = Multibinder.newSetBinder(binder(), RequestHandler.class);
    binder.addBinding().to(LoginHandler.class);
  }
}
