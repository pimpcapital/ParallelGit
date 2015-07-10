package com.beijunyi.parallelgit.filesystems;

import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GitUriParams extends HashMap<String, String> {

  public final static String SESSION_KEY = "session";
  public final static String BRANCH_KEY = "branch";
  public final static String REVISION_KEY = "revision";
  public final static String TREE_KEY = "tree";

  public void setSession(@Nonnull String session) {
    put(SESSION_KEY, session);
  }

  @Nullable
  public String getSession() {
    return get(SESSION_KEY);
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
