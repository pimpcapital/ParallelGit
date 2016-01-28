package com.beijunyi.parallelgit.web.connection;

import javax.annotation.Nonnull;
import javax.websocket.*;

import com.google.gson.Gson;

public class JsonEncoder implements Encoder.Text<TitledMessage> {

  private static final Gson MAPPER = new Gson();

  @Nonnull
  @Override
  public String encode(@Nonnull TitledMessage obj) throws EncodeException {
    return MAPPER.toJson(obj);
  }

  @Override
  public void init(@Nonnull EndpointConfig config) {
  }

  @Override
  public void destroy() {
  }

}
