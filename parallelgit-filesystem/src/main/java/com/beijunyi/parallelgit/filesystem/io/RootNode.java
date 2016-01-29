package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;

public class RootNode extends DirectoryNode {

  public RootNode(@Nullable AnyObjectId id, @Nonnull GfsObjectService objService) {
    super(id, objService);
  }

  @Nonnull
  public static RootNode fromObject(@Nonnull AnyObjectId id, @Nonnull GfsObjectService objService) {
    return new RootNode(id, objService);
  }

  @Nonnull
  public static RootNode newRoot(@Nonnull GfsObjectService objService) {
    return new RootNode(null, objService);
  }

  @Nonnull
  @Override
  public AnyObjectId getObjectId(boolean persist) throws IOException {
    return super.getObjectId(persist);
  }

  @Nonnull
  @Override
  public TreeSnapshot getSnapshot(boolean persist) throws IOException {
    return super.getSnapshot(persist);
  }

  @Nullable
  @Override
  protected TreeSnapshot takeSnapshot(boolean persist) throws IOException {
    return takeSnapshot(persist, true);
  }
}
