package com.beijunyi.parallelgit.web.workspace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.web.connection.MessageData;

public class DataUpdate {

  private final String type;
  private final String target;
  private final String value;

  public DataUpdate(@Nonnull String type, @Nullable String target, @Nonnull String value) {
    this.type = type;
    this.target = target;
    this.value = value;
  }

  public DataUpdate(@Nonnull MessageData msg) {
    this(msg.getString("type"), msg.getString("target"), msg.getString("value"));
  }

  @Nonnull
  public String getType() {
    return type;
  }

  @Nullable
  public String getTarget() {
    return target;
  }

  @Nonnull
  public String getValue() {
    return value;
  }

}
