package com.beijunyi.parallelgit.web.protocol;

import java.util.Arrays;
import java.util.Collection;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ProtocolModule extends AbstractModule {

  private static final Collection<Class<? extends RequestHandler>> HANDLERS = Arrays.asList(
    CheckoutRequestHandler.class,
    ListBranchesHandler.class,
    LoginHandler.class
  );

  @Override
  protected void configure() {
    Multibinder<RequestHandler> binder = Multibinder.newSetBinder(binder(), RequestHandler.class);
    for(Class<? extends RequestHandler> handler : HANDLERS)
      binder.addBinding().to(handler);
  }
}
