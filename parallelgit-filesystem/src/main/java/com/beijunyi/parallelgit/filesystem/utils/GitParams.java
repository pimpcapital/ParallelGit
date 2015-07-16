package com.beijunyi.parallelgit.filesystem.utils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;

public class GitParams extends HashMap<String, String> {

  public final static String BRANCH_KEY = "branch";
  public final static String REVISION_KEY = "revision";
  public final static String TREE_KEY = "tree";

  @Nonnull
  public static GitParams emptyMap() {
    return new GitParams();
  }

  @Nonnull
  public static GitParams getParams(@Nonnull Map<String, ?> properties) {
    GitParams params = new GitParams();
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
  public GitParams setBranch(@Nullable String branch) {
    if(branch != null)
      put(BRANCH_KEY, branch);
    else
      remove(BRANCH_KEY);
    return this;
  }

  @Nullable
  public String getBranch() {
    return get(BRANCH_KEY);
  }

  @Nonnull
  public GitParams setRevision(@Nullable String revision) {
    if(revision != null)
      put(REVISION_KEY, revision);
    else
      remove(REVISION_KEY);
    return this;
  }

  @Nullable
  public String getRevision() {
    return get(REVISION_KEY);
  }

  @Nonnull
  public GitParams setTree(@Nullable String tree) {
    if(tree != null)
      put(TREE_KEY, tree);
    else
      remove(TREE_KEY);
    return this;
  }

  @Nullable
  public String getTree() {
    return get(TREE_KEY);
  }

}
