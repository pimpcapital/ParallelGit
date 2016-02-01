package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;

import static org.eclipse.jgit.lib.FileMode.TREE;

public class RootNode extends DirectoryNode {

  public RootNode(@Nonnull AnyObjectId id, @Nonnull GfsObjectService objService) {
    super(id, objService);
  }

  public RootNode(@Nonnull GfsObjectService objService) {
    super(objService);
  }

  @Nonnull
  public static RootNode fromCommit(@Nonnull RevCommit commit, @Nonnull GfsObjectService objService) {
    return new RootNode(commit.getTree(), objService);
  }

  @Nonnull
  public static RootNode newRoot(@Nonnull GfsObjectService objService) {
    return new RootNode(objService);
  }

  @Nonnull
  @Override
  public AnyObjectId getObjectId(boolean persist) throws IOException {
    AnyObjectId ret = super.getObjectId(persist);
    assert ret != null;
    return ret;
  }

  @Override
  protected boolean isTrivial(@Nonnull Map<String, Node> data) {
    return false;
  }
}
