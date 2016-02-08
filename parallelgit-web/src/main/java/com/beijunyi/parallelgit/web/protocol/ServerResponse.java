package com.beijunyi.parallelgit.web.protocol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerResponse {

  private final String rid;
  private final boolean successful;
  private final Object data;

  public ServerResponse(@Nonnull String rid, boolean successful, @Nullable Object data) {
    this.rid = rid;
    this.successful = successful;
    this.data = data;
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
