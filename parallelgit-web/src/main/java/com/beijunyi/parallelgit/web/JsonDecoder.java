package com.beijunyi.parallelgit.web;

import javax.annotation.Nonnull;
import javax.websocket.*;

public class JsonDecoder implements Decoder.Text<Message> {

  @Override
  public Message decode(@Nonnull String s) throws DecodeException {
    return null;
  }

  @Override
  public boolean willDecode(@Nonnull String s) {
    return false;
  }

  @Override
  public void init(EndpointConfig config) {

  }

  @Override
  public void destroy() {

  }

}
