package com.beijunyi.parallelgit.web.connection;

import javax.annotation.Nonnull;
import javax.websocket.server.ServerEndpointConfig;

import com.beijunyi.parallelgit.web.config.InjectorFactory;

public class WebSocketEndPointConfigurator extends ServerEndpointConfig.Configurator {

  @Override
  public <T> T getEndpointInstance(@Nonnull Class<T> clazz) throws InstantiationException {
    return InjectorFactory.getInstance().getInstance(clazz);
  }

}
