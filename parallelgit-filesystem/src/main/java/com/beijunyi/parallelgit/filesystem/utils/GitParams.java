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

  public void setBranch(@Nonnull String branch) {
    put(BRANCH_KEY, branch);
  }

  @Nullable
  public String getBranch() {
    return get(BRANCH_KEY);
  }

  public void setRevision(@Nonnull String revision) {
    put(REVISION_KEY, revision);
  }

  @Nullable
  public String getRevision() {
    return get(REVISION_KEY);
  }

  public void setTree(@Nonnull String tree) {
    put(TREE_KEY, tree);
  }

  @Nullable
  public String getTree() {
    return get(TREE_KEY);
  }

}
