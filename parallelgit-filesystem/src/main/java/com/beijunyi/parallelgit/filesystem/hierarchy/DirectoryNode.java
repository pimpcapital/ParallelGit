package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.io.GitDirectoryStream;
import com.beijunyi.parallelgit.filesystem.io.GitSeekableByteChannel;
import com.beijunyi.parallelgit.filesystem.utils.GitCopyOption;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public class DirectoryNode extends Node {

  private Map<String, Node> children;
  private final Collection<GitDirectoryStream> streams = new LinkedList<>();

  protected DirectoryNode(@Nonnull GitPath root, @Nonnull AnyObjectId object, @Nonnull Repository repository) {
    super(NodeType.DIRECTORY, object);
    this.path = root;
    this.repository = repository;
  }

  protected DirectoryNode(@Nonnull GitPath root, @Nonnull Repository repository) {
    this(root, ObjectId.zeroId(), repository);
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
  public static DirectoryNode newRoot(@Nonnull GitPath rootPath, @Nullable AnyObjectId treeId, @Nonnull Repository repository) {
    return treeId != null ? new DirectoryNode(rootPath, treeId, repository) : new DirectoryNode(rootPath, repository);
  }

  @Override
  protected void doLoad(boolean recursive) throws IOException {
    children = new HashMap<>();
    TreeWalk tw = new TreeWalk(repository);
    try {
      while(tw.next()) {
        AnyObjectId object = tw.getObjectId(0);
        FileMode mode = tw.getFileMode(0);
        Node node;
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
        addChild(tw.getNameString(), node);
        if(recursive)
          node.load(true);
      }
    } finally {
      tw.release();
    }
  }

  @Override
  protected boolean doUnload(boolean recursive) {
    if(!dirty) {
      children = null;
      return true;
    } else if(recursive) {
      for(Node child : children.values())
        child.unload(true);
    }
    return false;
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
    load(false);
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
    checkLocked();
    load(false);
    return children.get(name);
  }

  @Nonnull
  public synchronized GitSeekableByteChannel openChild(@Nonnull String name, @Nonnull Set<OpenOption> options) throws IOException {
    Node child = children.get(name);
    if(child == null) {
      if(!options.contains(StandardOpenOption.CREATE) && !options.contains(StandardOpenOption.CREATE_NEW))
        throw new NoSuchFileException(getPath().resolve(name).toString());
      child = FileNode.newFile();
      addChild(name, child);
    } else if(options.contains(StandardOpenOption.CREATE_NEW))
      throw new FileAlreadyExistsException(child.getPath().toString());
    if(child.isDirectory())
      throw new AccessDeniedException(child.getPath().toString());
    return child.asFile().newChannel(options);
  }

  private void addChild(@Nonnull String name, @Nonnull Node node) {
    node.name = name;
    node.parent = this;
    node.path = null;
    children.put(name, node);
    invalidateSize();
    markAncestorDirty();
  }

  public synchronized void addChild(@Nonnull String name, @Nonnull Node node, @Nonnull Set<CopyOption> options) throws IOException {
    load(false);
    if(children.get(name) != null) {
      if(options.contains(StandardCopyOption.REPLACE_EXISTING))
        removeChild(name);
    } else
      throw new FileAlreadyExistsException(getPath().resolve(name).toString());
    if(options.contains(GitCopyOption.LOAD_BEFORE_COPY))
      node.load(true);
    Node toAdd = options.contains(GitCopyOption.CLONE) ? node.makeClone() : node;
    if(options.contains(GitCopyOption.MARK_DIRTY))
      toAdd.markDirty(true);
    addChild(name, toAdd);
    if(options.contains(GitCopyOption.UNLOAD_AFTER_COPY))
      node.unload(true);
  }

  public synchronized void removeChild(@Nonnull String name) throws IOException {
    load(false);
    children.remove(name);
  }

  @Override
  public void markDirty(boolean recursive) {
    checkLoaded();
    dirty = true;
    if(recursive) {
      for(Node child : children.values())
        child.markDirty(true);
    }
  }

  @Nonnull
  @Override
  protected synchronized DirectoryNode prepareClone() throws AccessDeniedException {
    DirectoryNode clone = new DirectoryNode(object);
    clone.children = new HashMap<>();
    for(Map.Entry<String, Node> child : children.entrySet()) {
      Node node = child.getValue();
      node.checkLocked();
      clone.addChild(child.getKey(), node);
    }
    return clone;
  }
}
