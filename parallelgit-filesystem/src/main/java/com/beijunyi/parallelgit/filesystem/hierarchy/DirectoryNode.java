package com.beijunyi.parallelgit.filesystem.hierarchy;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.io.GitDirectoryStream;
import com.beijunyi.parallelgit.filesystem.io.GitSeekableByteChannel;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;

public class DirectoryNode extends Node {

  private Map<String, Node> children;
  private final Collection<GitDirectoryStream> streams = new LinkedList<>();

  protected DirectoryNode(@Nonnull GitPath root, @Nonnull AnyObjectId object, @Nonnull ObjectReader reader) {
    super(NodeType.DIRECTORY, object);
    path = root;
    this.reader = reader;
  }

  protected DirectoryNode(@Nonnull GitPath root, @Nonnull ObjectReader reader) {
    this(root, ObjectId.zeroId(), reader);
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
  public static DirectoryNode newRoot(@Nonnull GitPath rootPath, @Nullable AnyObjectId treeId, @Nonnull ObjectReader reader) {
    return treeId != null ? new DirectoryNode(rootPath, treeId, reader) : new DirectoryNode(rootPath, reader);
  }

  @Override
  protected void doLoad() throws IOException {
    children = new HashMap<>();
    TreeWalk tw = new TreeWalk(reader);
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
      }
    } finally {
      tw.release();
    }
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
  public synchronized Node findNode(@Nonnull GitPath path) throws IOException {
    if(path.isAbsolute())
      path = getPath().relativize(path);
    if(path.isEmpty())
      return this;
    String childName = path.getName(0).toString();
    load();
    Node child = children.get(childName);
    if(child != null && child.isDirectory() && path.getNameCount() > 1)
      return child.asDirectory().findNode(path.subpath(1, path.getNameCount()));
    return child;
  }

  @Nonnull
  private Node ensureChild(@Nonnull String name) throws NoSuchFileException, AccessDeniedException {
    Node child = children.get(name);
    if(child == null)
      throw new NoSuchFileException(getPath().resolve(name).toString());
    return child;
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
    markDirty();
  }

  public synchronized void addChild(@Nonnull String name, @Nonnull Node node, boolean clone, boolean replace) throws IOException {
    load();
    if(children.get(name) != null) {
      if(replace)
        deleteChild(name);
    } else
      throw new FileAlreadyExistsException(getPath().resolve(name).toString());
    addChild(name, clone ? node.makeClone() : node);
  }

  public void addDirectory(@Nonnull String name, boolean replace) throws IOException {
    addChild(name, DirectoryNode.newDirectory(), false, replace);
  }

  public void copyChild(@Nonnull String name, @Nonnull DirectoryNode targetDirectory, @Nonnull String newName, boolean replace) throws IOException {
    load();
    Node child = ensureChild(name);
    targetDirectory.addChild(newName, child, true, replace);
  }

  public synchronized void moveChild(@Nonnull String name, @Nonnull DirectoryNode targetDirectory, @Nonnull String newName, boolean replace) throws IOException {
    load();
    Node child = ensureChild(name);
    child.lock();
    try {
      targetDirectory.addChild(newName, child, false, replace);
      children.remove(name);
    } finally {
      child.unlock();
    }
    invalidateSize();
    markDirty();
  }

  public synchronized void renameChild(@Nonnull String name, @Nonnull String newName, boolean replace) throws IOException {
    if(name.equals(newName))
      return;
    load();
    Node child = ensureChild(name);
    child.lock();
    try {
      addChild(newName, child, false, replace);
      children.remove(name);
    } finally {
      child.unlock();
    }
    markDirty();
  }

  public synchronized void deleteChild(@Nonnull String name) throws IOException {
    load();
    Node child = ensureChild(name);
    child.lock();
    children.remove(name);
    invalidateSize();
    markDirty();
  }

  @Nonnull
  @Override
  protected synchronized DirectoryNode prepareClone() {
    DirectoryNode clone = new DirectoryNode(object);
    clone.children = new HashMap<>();
    for(Map.Entry<String, Node> child : children.entrySet())
      clone.addChild(child.getKey(), child.getValue());
    return clone;
  }
}
