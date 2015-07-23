package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.TreeWalk;

public class DirectoryNode extends TreeNode {

  private Map<String, TreeNode> children;

  public DirectoryNode() {
    super(TreeNodeType.DIRECTORY);
  }

  @Nonnull
  public static DirectoryNode forTreeObject(@Nonnull AnyObjectId object) {
    DirectoryNode node = new DirectoryNode();
    node.object = object;
    return node;
  }

  @Override
  protected void doLoad(@Nonnull ObjectReader reader) throws IOException {
    children = new HashMap<>();
    TreeWalk tw = new TreeWalk(reader);
    try {
      while(tw.next()) {
        AnyObjectId object = tw.getObjectId(0);
        FileMode mode = tw.getFileMode(0);
        TreeNode node;
        if(mode.equals(FileMode.TREE))
          node = DirectoryNode.forTreeObject(object);
        else if(mode.equals(FileMode.REGULAR_FILE))
          node = FileNode.forRegularFileObject(object);
        else if(mode.equals(FileMode.EXECUTABLE_FILE))
          node = FileNode.forExecutableFileObject(object);
        else if(mode.equals(FileMode.SYMLINK))
          node = FileNode.forSymlinkBlob(object);
        else
          throw new UnsupportedOperationException("File mode " + mode + " is not supported");
        children.put(tw.getNameString(), node);
      }
    } finally {
      tw.release();
    }
  }

  @Override
  synchronized public void lock() throws AccessDeniedException {
    if(children != null) {
      List<TreeNode> lockedChildren = new LinkedList<>();
      boolean failed = false;
      try {
        for(TreeNode child : children.values()) {
          child.lock();
          lockedChildren.add(child);
        }
      } catch(AccessDeniedException e) {
        failed = true;
        throw e;
      } finally {
        if(failed) {
          for(TreeNode child : lockedChildren)
            child.unlock();
        }
      }
    }
    super.lock();
  }

  @Override
  synchronized public void unlock() throws IllegalStateException {
    super.unlock();
    if(children != null)
      for(TreeNode child : children.values())
        child.unlock();
  }

}
