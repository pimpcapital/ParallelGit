package com.beijunyi.parallelgit.web;

import javax.annotation.Nonnull;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class JsonEncoder implements Encoder.Text<Message> {

  @Nonnull
  @Override
  public String encode(@Nonnull Message object) throws EncodeException {
    return null;
  }

  @Override
  public void init(EndpointConfig config) {

  }

  @Override
  public void destroy() {

  }

}
