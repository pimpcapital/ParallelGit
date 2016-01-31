package com.beijunyi.parallelgit.web.connection;

import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessageData extends HashMap<String, Object> {

  public MessageData() {
  }

  public MessageData(@Nonnull String rid, @Nonnull Object data) {
    put("rid", rid);
    put("data", data);
  }

  @Nonnull
  public String getString(@Nonnull String key) {
    return (String) get(key);
  }

}
