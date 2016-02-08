package com.beijunyi.parallelgit.web.connection;

import javax.annotation.Nonnull;
import javax.websocket.*;

import com.beijunyi.parallelgit.web.config.GsonFactory;
import com.beijunyi.parallelgit.web.protocol.ClientRequest;
import com.google.gson.Gson;

public class JsonEncoder implements Encoder.Text<ClientRequest> {

  private final Gson gson = GsonFactory.getInstance();

  @Nonnull
  @Override
  public String encode(@Nonnull ClientRequest obj) throws EncodeException {
    return gson.toJson(obj);
  }

  @Override
  public void init(@Nonnull EndpointConfig config) {
  }

  @Override
  public void destroy() {
  }

}
