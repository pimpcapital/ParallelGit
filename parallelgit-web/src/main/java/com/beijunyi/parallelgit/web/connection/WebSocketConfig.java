package com.beijunyi.parallelgit.web.connection;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

public class WebSocketConfig implements ServerApplicationConfig {

  @Nonnull
  @Override
  public Set<ServerEndpointConfig> getEndpointConfigs(@Nonnull Set<Class<? extends Endpoint>> scanned) {
    return Collections.emptySet();
  }

  @Nonnull
  @Override
  public Set<Class<?>> getAnnotatedEndpointClasses(@Nonnull Set<Class<?>> scanned) {
    return scanned;
  }

}
