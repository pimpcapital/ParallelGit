package com.beijunyi.parallelgit.filesystem.utils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;

public class GfsParams extends HashMap<String, String> {

  public final static String CREATE_KEY = "create";
  public final static String BARE_KEY = "bare";
  public final static String BRANCH_KEY = "branch";
  public final static String COMMIT_KEY = "commit";

  @Nonnull
  public static GfsParams emptyMap() {
    return new GfsParams();
  }

  @Nonnull
  public static GfsParams getParams(@Nonnull Map<String, ?> properties) {
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
  public GfsParams setCreate(@Nullable String create) {
    if(create != null)
      put(CREATE_KEY, create);
    return this;
  }

  @Nonnull
  public GfsParams setCreate(boolean create) {
    return setCreate(Boolean.toString(create));
  }

  @Nullable
  public Boolean getCreate() {
    String value = get(CREATE_KEY);
    return value != null ? Boolean.valueOf(value) : null;
  }

  @Nonnull
  public GfsParams setBare(@Nullable String bare) {
    if(bare != null)
      put(BARE_KEY, bare);
    return this;
  }

  @Nonnull
  public GfsParams setBare(boolean bare) {
    return setBare(Boolean.toString(bare));
  }

  @Nullable
  public Boolean getBare() {
    String value = get(BARE_KEY);
    return value != null ? Boolean.valueOf(value) : null;
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
