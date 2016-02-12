package com.beijunyi.parallelgit.web.connection;

import javax.annotation.Nonnull;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import com.beijunyi.parallelgit.web.config.InjectorFactory;
import org.apache.shiro.SecurityUtils;

public class EndPointConfigurator extends ServerEndpointConfig.Configurator {

  @Override
  public <T> T getEndpointInstance(@Nonnull Class<T> clazz) throws InstantiationException {
    return InjectorFactory.getInstance().getInstance(clazz);
  }

  @Override
  public void modifyHandshake(@Nonnull ServerEndpointConfig sec, @Nonnull HandshakeRequest request, @Nonnull HandshakeResponse response) {
    sec.getUserProperties().put("Subject", SecurityUtils.getSubject());
  }
}
