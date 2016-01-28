package com.beijunyi.parallelgit.web.workspace;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.web.connection.MessageData;

public class ResourceRequest {

  private final String type;
  private final String rid;

  public ResourceRequest(@Nonnull String type, @Nonnull String rid) {
    this.type = type;
    this.rid = rid;
  }

  public ResourceRequest(@Nonnull MessageData msg) {
    this(msg.getString("type"), msg.getString("rid"));
  }

  @Nonnull
  public String getType() {
    return type;
  }

  @Nonnull
  public String getRequestId() {
    return rid;
  }

}
