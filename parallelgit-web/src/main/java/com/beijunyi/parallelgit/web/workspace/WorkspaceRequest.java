package com.beijunyi.parallelgit.web.workspace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.web.connection.MessageData;

public class WorkspaceRequest {

  private final String rid;
  private final String type;
  private final String target;
  private final String value;

  public WorkspaceRequest(@Nonnull String rid, @Nonnull String type, @Nullable String target, @Nullable String value) {
    this.rid = rid;
    this.type = type;
    this.target = target;
    this.value = value;
  }

  public WorkspaceRequest(@Nonnull MessageData msg) {
    this(msg.getString("rid"), msg.getString("type"), msg.getString("target"), msg.getString("value"));
  }

  @Nonnull
  public String getType() {
    return type;
  }

  @Nonnull
  public String getRequestId() {
    return rid;
  }

  @Nonnull
  public String getTarget() {
    if(target == null)
      throw new IllegalStateException();
    return target;
  }

  @Nonnull
  public String getValue() {
    if(value == null)
      throw new IllegalStateException();
    return value;
  }
}
