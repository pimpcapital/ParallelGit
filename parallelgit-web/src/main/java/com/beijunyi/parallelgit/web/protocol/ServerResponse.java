package com.beijunyi.parallelgit.web.protocol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerResponse {

  private final String rid;
  private final boolean successful;
  private final Object data;

  private ServerResponse(@Nonnull String rid, boolean successful, @Nullable Object data) {
    this.rid = rid;
    this.successful = successful;
    this.data = data;
  }

  @Nonnull
  public static ServerResponse ok(@Nonnull String rid, @Nullable Object data) {
    return new ServerResponse(rid, true, data);
  }

  @Nonnull
  public static ServerResponse error(@Nonnull String rid, @Nonnull String message) {
    return new ServerResponse(rid, false, message);
  }

  @Nonnull
  public String getRid() {
    return rid;
  }

  public boolean isSuccessful() {
    return successful;
  }

  @Nullable
  public Object getData() {
    return data;
  }

}
