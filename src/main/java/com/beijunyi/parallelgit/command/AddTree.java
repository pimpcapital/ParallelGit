package com.beijunyi.parallelgit.command;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.util.DirCacheHelper;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;

class AddTree extends CacheEditor {
  private AnyObjectId treeId;
  private String treeIdStr;

  AddTree(@Nonnull String path) {
    super(path);
  }

  void setTreeId(@Nullable AnyObjectId treeId) {
    this.treeId = treeId;
  }

  void setTreeIdStr(@Nullable String treeIdStr) {
    this.treeIdStr = treeIdStr;
  }

  @Override
  protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
    ObjectReader reader = provider.getReader();
    if(treeId == null)
      treeId = provider.getRepository().resolve(treeIdStr);
    DirCacheBuilder builder = provider.getCurrentBuilder();
    DirCacheHelper.addTree(builder, reader, path, treeId);
  }
}