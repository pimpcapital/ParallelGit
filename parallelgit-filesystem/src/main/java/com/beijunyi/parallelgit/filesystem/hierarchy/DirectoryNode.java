package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitCopyOption;
import com.beijunyi.parallelgit.filesystem.GitFileStore;
import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.io.GitDirectoryStream;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.treewalk.TreeWalk;

public class DirectoryNode extends Node {

  private Map<String, Node> children;
  private final Collection<GitDirectoryStream> streams = new LinkedList<>();

  protected DirectoryNode(@Nonnull AnyObjectId object, @Nonnull GitPath root, @Nonnull GitFileStore store) {
    super(NodeType.DIRECTORY, object, root, store);
  }

  protected DirectoryNode(@Nonnull GitPath root, @Nonnull GitFileStore store) {
    this(ObjectId.zeroId(), root, store);
    children = new HashMap<>();
    loaded = true;
    dirty = true;
  }

  protected DirectoryNode(@Nonnull AnyObjectId object) {
    super(NodeType.DIRECTORY, object);
  }

  protected DirectoryNode() {
    this(ObjectId.zeroId());
    children = new HashMap<>();
    loaded = true;
    dirty = true;
  }

  @Nonnull
  public static DirectoryNode forTreeObject(@Nonnull AnyObjectId object) {
    return new DirectoryNode(object);
  }

  @Nonnull
  public static DirectoryNode newDirectory() {
    return new DirectoryNode();
  }

  @Nonnull
  public static DirectoryNode newRoot(@Nonnull GitPath rootPath, @Nullable AnyObjectId treeId, @Nonnull GitFileStore store) {
    return treeId != null ? new DirectoryNode(treeId, rootPath, store) : new DirectoryNode(rootPath, store);
  }

  @Override
  protected void load(@Nonnull GitFileStore store, boolean recursive) throws IOException {
    if(!loaded) {
      children = new HashMap<>();
      TreeWalk tw = store.newTreeWalk();
      try {
        while(tw.next()) {
          Node node = forObject(tw.getObjectId(0), tw.getFileMode(0));
          addChild(tw.getNameString(), node);
        }
      } finally {
        tw.release();
      }
      loaded = true;
    }
    if(recursive) {
      for(Node child : children.values())
        child.load(store, true);
      dirty = true;
    }
  }

  @Nonnull
  @Override
  public AnyObjectId save() throws IOException {
    if(!dirty)
      return object;
    TreeFormatter formatter = new TreeFormatter();
    for(Map.Entry<String, Node> child : new TreeMap<>(children).entrySet()) {
      String name = child.getKey();
      Node node = child.getValue();
      formatter.append(name, node.getType().toFileMode(), node.save());
    }
    return store().insertTree(formatter);
  }

  @Override
  public synchronized void lock() throws AccessDeniedException {
    if(children != null) {
      List<Node> lockedChildren = new LinkedList<>();
      boolean failed = false;
      try {
        for(Node child : children.values()) {
          child.lock();
          lockedChildren.add(child);
        }
      } catch(AccessDeniedException e) {
        failed = true;
        throw e;
      } finally {
        if(failed) {
          for(Node child : lockedChildren)
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
      for(Node child : children.values())
        child.unlock();
  }

  @Override
  protected long calculateSize() throws IOException {
    load();
    long total = 0;
    for(Node child : children.values())
      total += child.getSize();
    return total;
  }

  @Nonnull
  public synchronized GitDirectoryStream newStream(@Nullable DirectoryStream.Filter<? super Path> filter) throws AccessDeniedException {
    if(locked)
      denyAccess();
    List<Node> nodes = new ArrayList<>(children.values());
    Collections.sort(nodes);
    GitDirectoryStream stream = new GitDirectoryStream(nodes.iterator(), filter, this);
    streams.add(stream);
    return stream;
  }

  public synchronized void removeStream(@Nonnull GitDirectoryStream stream) {
    if(!streams.remove(stream))
      throw new IllegalArgumentException();
  }

  @Nullable
  public Node getChild(@Nonnull String name) throws IOException {
    checkNotLocked();
    load();
    return children.get(name);
  }

  private void addChild(@Nonnull String name, @Nonnull Node child) {
    child.name = name;
    child.parent = this;
    children.put(name, child);
    markDirty();
  }

  public synchronized void addChild(@Nonnull String name, @Nonnull Node child, @Nonnull Set<CopyOption> options) throws IOException {
    load();
    if(children.get(name) != null) {
      if(options.contains(StandardCopyOption.REPLACE_EXISTING))
        removeChild(name);
    } else
      throw new FileAlreadyExistsException(path().resolve(name).toString());
    addChild(name, options.contains(GitCopyOption.CLONE) ? child.clone(options.contains(GitCopyOption.DEEP_CLONE)) : child);
  }

  public void addNewFile(@Nonnull String name, boolean executable) {
    addChild(name, FileNode.newFile(executable));
  }

  public synchronized void removeChild(@Nonnull String name) throws IOException {
    load();
    children.remove(name);
  }

  @Nonnull
  @Override
  protected synchronized DirectoryNode clone(boolean deepClone) throws IOException {
    DirectoryNode clone = new DirectoryNode(object);
    if(deepClone || dirty) {
      if(loaded) {
        clone.children = new HashMap<>();
        for(Map.Entry<String, Node> child : children.entrySet())
          clone.children.put(child.getKey(), child.getValue().clone(deepClone));
        clone.loaded = true;
        clone.dirty = true;
      } else
        clone.load(store(), true);
    }
    return clone;
  }
}
