package com.beijunyi.parallelgit.commands.cache;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.CacheHelper;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;

public class AddTree extends CacheEditor {
  private AnyObjectId treeId;
  private String treeIdStr;

  public AddTree(@Nonnull String path) {
    super(path);
  }

  public void setTreeId(@Nullable AnyObjectId treeId) {
    this.treeId = treeId;
  }

  public void setTreeIdStr(@Nullable String treeIdStr) {
    this.treeIdStr = treeIdStr;
  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    ObjectReader reader = provider.getReader();
    if(treeId == null)
      treeId = provider.getRepository().resolve(treeIdStr);
    DirCacheBuilder builder = provider.getCurrentBuilder();
    CacheHelper.addTree(builder, reader, path, treeId);
  }
}