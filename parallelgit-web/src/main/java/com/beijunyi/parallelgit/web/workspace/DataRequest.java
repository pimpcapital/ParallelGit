package com.beijunyi.parallelgit.web.workspace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.web.connection.MessageData;

public class DataRequest {

  private final String rid;
  private final String type;
  private final String target;

  public DataRequest(@Nonnull String rid, @Nonnull String type, @Nullable String target) {
    this.rid = rid;
    this.type = type;
    this.target = target;
  }

  public DataRequest(@Nonnull MessageData msg) {
    this(msg.getString("rid"), msg.getString("type"), msg.getString("target"));
  }

  @Nonnull
  public String getType() {
    return type;
  }

  @Nonnull
  public String getRequestId() {
    return rid;
  }

  @Nullable
  public String getTarget() {
    return target;
  }

}
