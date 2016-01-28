package com.beijunyi.parallelgit.web.connection;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws", decoders = JsonDecoder.class, encoders = JsonEncoder.class, configurator = WebSocketEndPointConfigurator.class)
public class WebSocketEndPoint {

  @OnMessage
  public void handleRequest(@Nonnull TitledMessage msg, @Nonnull Session session) throws EncodeException, IOException {
    session.getBasicRemote().sendObject(msg);
  }

}
