package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.TreeWalk;

public class DirectoryNode extends TreeNode {

  private Map<String, TreeNode> children;

  public DirectoryNode(@Nonnull ObjectReader reader) {
    super(TreeNodeType.DIRECTORY, reader);
  }

  @Nonnull
  public static DirectoryNode forTreeObject(@Nonnull AnyObjectId object, @Nonnull ObjectReader reader) {
    DirectoryNode node = new DirectoryNode(reader);
    node.object = object;
    return node;
  }

  @Nonnull
  public static DirectoryNode newDirectory(@Nullable String name, @Nullable DirectoryNode parent, @Nonnull ObjectReader reader) {
    DirectoryNode node = new DirectoryNode(reader);
    node.name = name;
    node.parent = parent;
    node.loaded = true;
    node.dirty = true;
    node.children = new HashMap<>();
    return node;
  }

  @Nonnull
  public static DirectoryNode newRoot(@Nonnull ObjectReader reader) {
    return newDirectory(null, null, reader);
  }

  @Override
  protected void doLoad() throws IOException {
    children = new HashMap<>();
    TreeWalk tw = new TreeWalk(reader);
    try {
      while(tw.next()) {
        AnyObjectId object = tw.getObjectId(0);
        FileMode mode = tw.getFileMode(0);
        TreeNode node;
        if(mode.equals(FileMode.TREE))
          node = DirectoryNode.forTreeObject(object, reader);
        else if(mode.equals(FileMode.REGULAR_FILE))
          node = FileNode.forRegularFileObject(object, reader);
        else if(mode.equals(FileMode.EXECUTABLE_FILE))
          node = FileNode.forExecutableFileObject(object, reader);
        else if(mode.equals(FileMode.SYMLINK))
          node = FileNode.forSymlinkBlob(object, reader);
        else
          throw new UnsupportedOperationException("File mode " + mode + " is not supported");
        children.put(tw.getNameString(), node);
      }
    } finally {
      tw.release();
    }
  }

  @Override
  public synchronized void lock() throws AccessDeniedException {
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
  public synchronized void unlock() throws IllegalStateException {
    super.unlock();
    if(children != null)
      for(TreeNode child : children.values())
        child.unlock();
  }

  @Nonnull
  public List<String> getChildren() throws IOException {
    load();
    List<String> ret = new ArrayList<>(children.keySet());
    Collections.sort(ret);
    return Collections.unmodifiableList(ret);
  }

  @Nullable
  public TreeNode findNode(@Nonnull String path) throws IOException {
    if(path.isEmpty())
      return this;
    load();
    int nameEnd = path.indexOf('/');
    String childName = nameEnd >= 0 ? path.substring(0, nameEnd) : path;
    TreeNode child = children.get(childName);
    if(child.isDirectory() && path.length() > childName.length())
      return ((DirectoryNode) child).findNode(path.substring(childName.length() + 1));
    return child;
  }

}
