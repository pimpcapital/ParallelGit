package com.beijunyi.parallelgit.web.protocol;

import java.util.Map;
import javax.annotation.Nonnull;

public class ClientRequest {

  private final String type;
  private final String rid;
  private final Map<String, String> data;

  public ClientRequest(@Nonnull String type, @Nonnull String rid, @Nonnull Map<String, String> data) {
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
    String ret = data.get(key);
    if(ret == null)
      throw new IllegalStateException();
    return ret;
  }

  @Nonnull
  public ResponseBuilder respond() {
    return new ResponseBuilder(rid);
  }



}
