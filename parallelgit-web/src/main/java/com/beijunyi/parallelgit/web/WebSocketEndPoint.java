package com.beijunyi.parallelgit.web;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws", decoders = {}, encoders = {})
public class WebSocketEndPoint {

  @OnOpen
  synchronized public void onOpen(@Nonnull Session session) throws IOException {

  }

  @OnMessage
  public void echoTextMessage(Session session, String msg, boolean last) {
    try {
      if (session.isOpen()) {
        session.getBasicRemote().sendText(msg, last);
      }
    } catch (IOException e) {
      try {
        session.close();
      } catch (IOException e1) {
        // Ignore
      }
    }
  }

  @OnMessage
  public void echoBinaryMessage(Session session, ByteBuffer bb, boolean last) {
    try {
      if (session.isOpen()) {
        session.getBasicRemote().sendBinary(bb, last);
      }
    } catch (IOException e) {
      try {
        session.close();
      } catch (IOException e1) {
        // Ignore
      }
    }
  }

  /**
   * Process a received pong. This is a NO-OP.
   *
   * @param pm    Ignored.
   */
  @OnMessage
  public void echoPongMessage(PongMessage pm) {
    // NO-OP
  }
}
