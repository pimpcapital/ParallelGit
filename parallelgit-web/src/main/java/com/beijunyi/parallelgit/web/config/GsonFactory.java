package com.beijunyi.parallelgit.web.config;

import javax.annotation.Nonnull;

import com.google.gson.*;


public class GsonFactory {

  @Nonnull
  public static Gson getInstance() {
    return GsonHolder.INSTANCE;
  }

  private static class GsonHolder {
    private static final Gson INSTANCE =
      new GsonBuilder()
        .create();
  }

}
