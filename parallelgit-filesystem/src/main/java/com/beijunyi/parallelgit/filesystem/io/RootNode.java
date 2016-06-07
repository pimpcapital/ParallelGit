package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;


public class RootNode extends DirectoryNode {

  public RootNode(ObjectId id, GfsObjectService objService) throws IOException {
    super(id, objService);
    updateOrigin(id);
  }

  public RootNode(GfsObjectService objService) {
    super(objService);
  }

  @Nonnull
  public static RootNode fromCommit(RevCommit commit, GfsObjectService objService) throws IOException {
    return new RootNode(commit.getTree(), objService);
  }

  @Nonnull
  public static RootNode newRoot(GfsObjectService objService) {
    return new RootNode(objService);
  }

  @Override
  protected boolean isTrivial(Map<String, Node> data) {
    return false;
  }
}
