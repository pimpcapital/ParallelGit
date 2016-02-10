package com.beijunyi.parallelgit.web.protocol;

import javax.annotation.Nonnull;

public class ResponseBuilder {

  private final String rid;

  public ResponseBuilder(@Nonnull String rid) {
    this.rid = rid;
  }

  @Nonnull
  public ServerResponse ok(@Nonnull Object data) {
    return ServerResponse.ok(rid, data);
  }

  @Nonnull
  public ServerResponse ok() {
    return ServerResponse.ok(rid, null);
  }

  @Nonnull
  public ServerResponse error(@Nonnull String message) {
    return ServerResponse.error(rid, message);
  }

}
