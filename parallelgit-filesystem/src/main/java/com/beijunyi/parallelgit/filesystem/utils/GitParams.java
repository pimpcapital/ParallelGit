package com.beijunyi.parallelgit.filesystem.utils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;

public class GitParams extends HashMap<String, String> {

  public final static String CREATE_KEY = "create";
  public final static String BARE_KEY = "bare";
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
  public GitParams setCreate(@Nullable String create) {
    if(create != null)
      put(CREATE_KEY, create);
    else
      remove(CREATE_KEY);
    return this;
  }

  @Nonnull
  public GitParams setCreate(boolean create) {
    return setCreate(Boolean.toString(create));
  }

  @Nullable
  public Boolean getCreate() {
    String value = get(CREATE_KEY);
    return value != null ? Boolean.valueOf(value) : null;
  }

  @Nonnull
  public GitParams setBare(@Nullable String bare) {
    if(bare != null)
      put(BARE_KEY, bare);
    else
      remove(BARE_KEY);
    return this;
  }

  @Nonnull
  public GitParams setBare(boolean bare) {
    return setBare(Boolean.toString(bare));
  }

  @Nullable
  public Boolean getBare() {
    String value = get(BARE_KEY);
    return value != null ? Boolean.valueOf(value) : null;
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

  @Nonnull
  public GitParams setRevision(@Nullable AnyObjectId revision) {
    return setRevision(revision != null ? revision.getName() : null);
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

  @Nonnull
  public GitParams setTree(@Nullable AnyObjectId tree) {
    return setTree(tree != null ? tree.getName() : null);
  }

  @Nullable
  public String getTree() {
    return get(TREE_KEY);
  }

}
