package com.beijunyi.parallelgit.web.protocol;

import java.util.Map;
import javax.annotation.Nonnull;

public class ClientRequest {

  private final String type;
  private final String rid;
  private final Map<String, Object> data;

  public ClientRequest(@Nonnull String type, @Nonnull String rid, @Nonnull Map<String, Object> data) {
    this.type = type;
    this.rid = rid;
    this.data = data;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  @Nonnull
  public String getRid() {
    return rid;
  }

  @Nonnull
  public String getString(@Nonnull String key) {
    Object value = data.get(key);
    if(value == null)
      throw new IllegalStateException();
    return String.class.cast(value);
  }

  @Nonnull
  public Boolean getBoolean(@Nonnull String key) {
    Object value = data.get(key);
    if(value == null)
      throw new IllegalStateException();
    return Boolean.class.cast(value);
  }

  @Nonnull
  public ResponseBuilder respond() {
    return new ResponseBuilder(rid);
  }



}
