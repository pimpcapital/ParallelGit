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

  protected Map<String, Node> children;
  private final Collection<GitDirectoryStream> streams = new LinkedList<>();

  protected DirectoryNode(@Nonnull AnyObjectId object, @Nonnull GitPath root, @Nonnull GitFileStore store) {
    super(NodeType.DIRECTORY, object, root, store);
  }

  private DirectoryNode(@Nonnull AnyObjectId object) {
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

  @Override
  protected void doLoad(@Nonnull GitFileStore store, boolean recursive) throws IOException {
    if(!loaded) {
      children = new HashMap<>();
      TreeWalk tw = store.newTreeWalk();
      try {
        tw.addTree(object);
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
        child.doLoad(store, true);
      dirty = true;
    }
  }

  @Nullable
  @Override
  public AnyObjectId doSave(boolean allowEmpty) throws IOException {
    if(!dirty)
      return object;
    TreeFormatter formatter = new TreeFormatter();
    int count = 0;
    for(Map.Entry<String, Node> child : new TreeMap<>(children).entrySet()) {
      String name = child.getKey();
      Node node = child.getValue();
      AnyObjectId childObject = node.save();
      if(childObject != null) {
        formatter.append(name, node.getType().toFileMode(), childObject);
        count++;
      }
    }
    if(allowEmpty || count != 0)
      return store().insertTree(formatter);
    return null;
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
      else
        throw new FileAlreadyExistsException(path().resolve(name).toString());
    }
    addChild(name, options.contains(GitCopyOption.CLONE) ? child.clone(options.contains(GitCopyOption.DEEP_CLONE)) : child);
  }

  public void addNewFile(@Nonnull String name, boolean executable) throws IOException {
    addChild(name, FileNode.newFile(executable), Collections.<CopyOption>emptySet());
  }

  public void addNewDirectory(@Nonnull String name) throws IOException {
    addChild(name, DirectoryNode.newDirectory(), Collections.<CopyOption>emptySet());
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
        clone.doLoad(store(), true);
    }
    return clone;
  }
}
