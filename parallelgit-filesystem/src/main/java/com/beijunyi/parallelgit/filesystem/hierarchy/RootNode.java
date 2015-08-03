package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileStore;
import com.beijunyi.parallelgit.filesystem.GitPath;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;

public class RootNode extends DirectoryNode {

  private RootNode(@Nonnull AnyObjectId object, @Nonnull GitPath root, @Nonnull GitFileStore store) {
    super(object, root, store);
  }

  private RootNode(@Nonnull GitPath root, @Nonnull GitFileStore store) {
    this(ObjectId.zeroId(), root, store);
    children = new HashMap<>();
    loaded = true;
    dirty = true;
  }

  @Nonnull
  public static RootNode newRoot(@Nonnull GitPath rootPath, @Nullable AnyObjectId treeId, @Nonnull GitFileStore store) {
    return treeId != null ? new RootNode(treeId, rootPath, store) : new RootNode(rootPath, store);
  }


  @Nonnull
  @Override
  public AnyObjectId save() throws IOException {
    object = doSave(true);
    if(object == null)
      throw new IllegalStateException();
    dirty = false;
    return object;
  }

}
