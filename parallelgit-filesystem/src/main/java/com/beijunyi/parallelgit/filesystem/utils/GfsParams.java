package com.beijunyi.parallelgit.filesystem.utils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;

public class GfsParams extends HashMap<String, String> {

  public final static String BRANCH_KEY = "branch";
  public final static String COMMIT_KEY = "commit";

  private GfsParams() {}

  @Nonnull
  public static GfsParams emptyMap() {
    return new GfsParams();
  }

  @Nonnull
  public static GfsParams fromProperties(@Nonnull Map<String, ?> properties) {
    GfsParams params = new GfsParams();
    for(Map.Entry<String, ?> entry : properties.entrySet()) {
      Object value = entry.getValue();
      if(value instanceof AnyObjectId)
        params.put(entry.getKey(), ((AnyObjectId) value).getName());
      else
        params.put(entry.getKey(), value.toString());
    }
    return params;
  }

  @Nonnull
  public GfsParams setBranch(@Nullable String branch) {
    if(branch != null)
      put(BRANCH_KEY, branch);
    return this;
  }

  @Nullable
  public String getBranch() {
    return get(BRANCH_KEY);
  }

  @Nonnull
  public GfsParams setCommit(@Nullable String commit) {
    if(commit != null)
      put(COMMIT_KEY, commit);
    return this;
  }

  @Nonnull
  public GfsParams setCommit(@Nullable AnyObjectId commit) {
    return setCommit(commit != null ? commit.getName() : null);
  }

  @Nullable
  public String getCommit() {
    return get(COMMIT_KEY);
  }

}
