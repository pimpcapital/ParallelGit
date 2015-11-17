package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.utils.FileAttributeReader;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public final class GfsIO {

  @Nonnull
  private static GitPath getParent(@Nonnull GitPath child) {
    if(child.isRoot())
      throw new IllegalArgumentException(child.toString());
    GitPath parent = child.getParent();
    if(parent == null)
      throw new IllegalStateException(child.toString());
    return parent;
  }

  @Nullable
  private static Node findNode(@Nonnull GitPath path) throws IOException {
    if(!path.isAbsolute())
      throw new IllegalArgumentException(path.toString());
    Node current = path.getFileStore().getRoot();
    for(int i = 0; i < path.getNameCount(); i++) {
      GitPath name = path.getName(i);
      if(current instanceof DirectoryNode)
        current = prepareDirectory((DirectoryNode) current, path.getFileSystem()).getChild(name.toString());
      else
        return null;
    }
    return current;
  }

  @Nonnull
  private static Node getNode(@Nonnull GitPath path) throws IOException {
    Node node = findNode(path);
    if(node == null)
      throw new NoSuchFileException(path.toString());
    return node;
  }

  @Nonnull
  private static FileNode asFile(@Nullable Node node, @Nonnull GitPath path) throws NoSuchFileException, AccessDeniedException {
    if(node == null)
      throw new NoSuchFileException(path.toString());
    if(node instanceof FileNode)
      return (FileNode) node;
    throw new AccessDeniedException(path.toString());
  }

  @Nonnull
  static FileNode findFile(@Nonnull GitPath file) throws IOException {
    return asFile(findNode(file), file);
  }

  @Nonnull
  private static DirectoryNode asDirectory(@Nullable Node node, @Nonnull GitPath path) throws NotDirectoryException {
    if(node instanceof DirectoryNode)
      return (DirectoryNode) node;
    throw new NotDirectoryException(path.toString());
  }

  @Nonnull
  static DirectoryNode findDirectory(@Nonnull GitPath dir) throws IOException {
    return asDirectory(findNode(dir), dir);
  }

  @Nonnull
  private static String getFileName(@Nonnull GitPath path) throws IOException {
    GitPath name = path.getFileName();
    assert name != null;
    return name.toString();
  }

  @Nonnull
  private static byte[] readBlobObject(@Nullable AnyObjectId blobId, @Nonnull GitFileSystem gfs) throws IOException {
    if(blobId == null)
      return new byte[0];
    return gfs.loadObject(blobId);
  }

  @Nonnull
  private static byte[] loadFileData(@Nonnull FileNode file, @Nonnull GitFileSystem gfs) throws IOException {
    byte[] bytes = readBlobObject(file.getObject(), gfs);
    file.loadContent(bytes);
    return bytes;
  }

  @Nonnull
  public static byte[] getFileData(@Nonnull FileNode file, @Nonnull GitFileSystem gfs) throws IOException {
    byte[] bytes = file.getBytes();
    if(bytes == null)
      bytes = loadFileData(file, gfs);
    return bytes;
  }

  private static long readBlobSize(@Nullable AnyObjectId blobId, @Nonnull GitFileSystem gfs) throws IOException {
    if(blobId == null)
      return 0;
    return gfs.getBlobSize(blobId);
  }

  private static long loadFileSize(@Nonnull FileNode file, @Nonnull GitFileSystem gfs) throws IOException {
    long size = readBlobSize(file.getObject(), gfs);
    file.setSize(size);
    return size;
  }

  @Nonnull
  private static ConcurrentMap<String, Node> readTreeObject(@Nullable AnyObjectId treeId, @Nonnull GitFileSystem gfs, @Nonnull DirectoryNode parent) throws IOException {
    ConcurrentMap<String, Node> children = new ConcurrentHashMap<>();
    if(treeId != null) {
      byte[] treeData = gfs.loadObject(treeId);
      CanonicalTreeParser treeParser = new CanonicalTreeParser();
      treeParser.reset(treeData);
      while(!treeParser.eof()) {
        children.put(treeParser.getEntryPathString(), Node.forObject(treeParser.getEntryObjectId(), treeParser.getEntryFileMode(), parent));
        treeParser.next();
      }
    }
    return children;
  }

  @Nonnull
  private static Map<String, Node> loadChildren(@Nonnull DirectoryNode dir, @Nonnull GitFileSystem gfs) throws IOException {
    ConcurrentMap<String, Node> children = readTreeObject(dir.getObject(), gfs, dir);
    dir.loadChildren(children);
    return children;
  }

  @Nonnull
  private static DirectoryNode prepareDirectory(@Nonnull DirectoryNode dir, @Nonnull GitFileSystem gfs) throws IOException {
    if(dir.getChildren() == null)
      loadChildren(dir, gfs);
    return dir;
  }

  @Nonnull
  public static Map<String, Node> getChildren(@Nonnull DirectoryNode dir, @Nonnull GitFileSystem gfs) throws IOException {
    Map<String, Node> children = prepareDirectory(dir, gfs).getChildren();
    assert children != null;
    return children;
  }

  @Nullable
  public static Node getChild(@Nonnull String name, @Nonnull DirectoryNode dir, @Nonnull GitFileSystem gfs) throws IOException {
    return getChildren(dir, gfs).get(name);
  }

  private static void addChild(@Nonnull String name, @Nonnull Node node, @Nonnull DirectoryNode dir, @Nonnull GitFileSystem gfs) throws IOException {
    if(!prepareDirectory(dir, gfs).addChild(name, node, false))
      throw new IllegalStateException();
  }

  @Nonnull
  public static Node addChild(@Nonnull String name, @Nonnull AnyObjectId id, @Nonnull FileMode mode, @Nonnull DirectoryNode dir, @Nonnull GitFileSystem gfs) throws IOException {
    Node ret = Node.forObject(id, mode, dir);
    addChild(name, ret, dir, gfs);
    return ret;
  }

  @Nonnull
  public static FileNode addChildFile(@Nonnull String name, @Nonnull byte[] bytes, @Nonnull FileMode mode, @Nonnull DirectoryNode dir, @Nonnull GitFileSystem gfs) throws IOException {
    FileNode ret = FileNode.forBytes(bytes, mode, dir);
    addChild(name, ret, dir, gfs);
    return ret;
  }

  @Nonnull
  public static DirectoryNode addChildDirectory(@Nonnull String name, @Nonnull DirectoryNode dir, @Nonnull GitFileSystem gfs) throws IOException {
    DirectoryNode ret = DirectoryNode.newDirectory(dir);
    addChild(name, ret, dir, gfs);
    return ret;
  }

  public static boolean removeChild(@Nonnull String name, @Nonnull DirectoryNode dir, @Nonnull GitFileSystem gfs) throws IOException {
    return prepareDirectory(dir, gfs).removeChild(name);
  }

  public static long getSize(@Nonnull Node node, @Nonnull GitFileSystem gfs) throws IOException {
    long size;
    if(node instanceof FileNode) {
      FileNode file = (FileNode) node;
      size = file.getSize();
      if(size == -1L)
        size = loadFileSize(file, gfs);
    } else
      size = 0;
    return size;
  }

  @Nonnull
  public static GfsSeekableByteChannel newByteChannel(@Nonnull GitPath file, @Nonnull Set<OpenOption> options, @Nonnull Collection<FileAttribute> attrs) throws IOException {
    if(file.isRoot())
      throw new AccessDeniedException(file.toString());
    FileNode node;
    if(options.contains(StandardOpenOption.CREATE) || options.contains(StandardOpenOption.CREATE_NEW)) {
      GitPath parent = getParent(file);
      DirectoryNode parentNode = prepareDirectory(findDirectory(parent), file.getFileSystem());
      String name = getFileName(file);
      if(options.contains(StandardOpenOption.CREATE_NEW) || !parentNode.hasChild(name)) {
        node = FileNode.newFile(FileAttributeReader.read(attrs).isExecutable(), parentNode);
        if(!parentNode.addChild(name, node, false))
          throw new FileAlreadyExistsException(file.toString());
      } else {
        node = asFile(parentNode.getChild(name), file);
      }
    } else
      node = findFile(file);
    return new GfsSeekableByteChannel(node, file.getFileSystem(), options);
  }

  @Nonnull
  public static GfsDirectoryStream newDirectoryStream(@Nonnull GitPath dir, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    DirectoryNode node = prepareDirectory(findDirectory(dir), dir.getFileSystem());
    return new GfsDirectoryStream(node, dir, filter);
  }

  public static void createDirectory(@Nonnull GitPath dir) throws IOException {
    if(dir.isRoot())
      throw new FileAlreadyExistsException(dir.toString());
    GitPath parent = getParent(dir);
    DirectoryNode parentNode = prepareDirectory(findDirectory(parent), dir.getFileSystem());
    if(!parentNode.addChild(getFileName(dir), DirectoryNode.newDirectory(parentNode), false))
      throw new FileAlreadyExistsException(dir.toString());
  }

  private static void copyFile(@Nonnull FileNode source, @Nonnull GitFileSystem sourceFs, @Nonnull FileNode target) throws IOException {
    byte[] bytes = source.getBytes();
    if(bytes == null) {
      assert source.getObject() != null;
      bytes = sourceFs.loadObject(source.getObject());
    }
    target.updateContent(bytes);
  }

  private static void copyDirectory(@Nonnull DirectoryNode source, @Nonnull GitFileSystem sourceFs, @Nonnull DirectoryNode target, @Nonnull GitFileSystem targetFs) throws IOException {
    ConcurrentMap<String, Node> children = source.getChildren();
    if(children == null)
      children = readTreeObject(source.getObject(), sourceFs, source);
    ConcurrentMap<String, Node> clonedChildren = new ConcurrentHashMap<>();
    for(Map.Entry<String, Node> child : children.entrySet()) {
      Node clonedChild = Node.cloneNode(child.getValue(), target);
      clonedChildren.put(child.getKey(), clonedChild);
      copyNode(child.getValue(), sourceFs, clonedChild, targetFs);
    }
    target.setChildren(clonedChildren);
  }

  private static void copyNode(@Nonnull Node source, @Nonnull GitFileSystem sourceFs, @Nonnull Node target, @Nonnull GitFileSystem targetFs) throws IOException {
    if(!source.isDirty() && source.getObject() != null && targetFs.hasObject(source.getObject())) {
      target.setObject(source.getObject());
      return;
    }
    if(source instanceof FileNode && target instanceof FileNode)
      copyFile((FileNode) source, sourceFs, (FileNode) target);
    else if(source instanceof DirectoryNode && target instanceof DirectoryNode)
      copyDirectory((DirectoryNode) source, sourceFs, (DirectoryNode) target, targetFs);
    else
      throw new IllegalStateException();
  }

  public static boolean copy(@Nonnull GitPath source, @Nonnull GitPath target, @Nonnull Set<CopyOption> options) throws IOException {
    Node sourceNode = getNode(source);
    if(source.equals(target))
      return false;
    if(target.isRoot())
      throw new FileAlreadyExistsException(target.toString());
    GitPath targetParent = getParent(target);
    DirectoryNode targetDirectory = prepareDirectory(findDirectory(targetParent), target.getFileSystem());
    Node targetNode = Node.cloneNode(sourceNode, targetDirectory);
    if(!targetDirectory.addChild(getFileName(target), targetNode, options.contains(StandardCopyOption.REPLACE_EXISTING)))
      throw new FileAlreadyExistsException(target.toString());
    copyNode(sourceNode, source.getFileSystem(), targetNode, target.getFileSystem());
    return true;
  }

  public static boolean move(@Nonnull GitPath source, @Nonnull GitPath target, @Nonnull Set<CopyOption> options) throws IOException {
    if(copy(source, target, options)) {
      delete(source);
      return true;
    }
    return false;
  }

  public static void delete(@Nonnull GitPath file) throws IOException {
    if(file.isRoot())
      throw new AccessDeniedException(file.toString());
    GitPath parent = getParent(file);
    DirectoryNode parentNode = prepareDirectory(findDirectory(parent), file.getFileSystem());
    if(!parentNode.removeChild(getFileName(file)))
      throw new NoSuchFileException(file.toString());
  }

  public static void checkAccess(@Nonnull GitPath path, @Nonnull Set<AccessMode> modes) throws IOException {
    Node node = getNode(path);
    if(modes.contains(AccessMode.EXECUTE) && !node.isExecutableFile())
      throw new AccessDeniedException(path.toString());
  }

  @Nullable
  public static <V extends FileAttributeView> V getFileAttributeView(@Nonnull GitPath path, @Nonnull Class<V> type) throws IOException, UnsupportedOperationException {
    Node node = findNode(path);
    if(node != null)
      return GfsFileAttributeView.forNode(node, path.getFileSystem(), type);
    return null;
  }

  @Nonnull
  private static AnyObjectId persistFile(@Nonnull FileNode file, @Nonnull GitFileSystem gfs) throws IOException {
    byte[] bytes = file.getBytes();
    if(bytes == null)
      throw new IllegalStateException();
    return gfs.saveBlob(bytes);
  }

  @Nullable
  private static AnyObjectId persistDirectory(@Nonnull DirectoryNode dir, boolean isRoot, @Nonnull GitFileSystem gfs) throws IOException {
    Map<String, Node> children = dir.getChildren();
    int count = 0;
    TreeFormatter formatter = new TreeFormatter();
    if(children != null) {
      for(Map.Entry<String, Node> child : new TreeMap<>(children).entrySet()) {
        String name = child.getKey();
        Node node = child.getValue();
        AnyObjectId nodeObject = persistNode(node, false, gfs);
        if(nodeObject != null) {
          formatter.append(name, node.getMode(), nodeObject);
          count++;
        }
      }
    }
    return (isRoot || count != 0) ? gfs.saveTree(formatter) : null;
  }

  @Nullable
  private static AnyObjectId persistNode(@Nonnull Node node, boolean isRoot, @Nonnull GitFileSystem gfs) throws IOException {
    if(!node.isDirty())
      return node.getObject();
    AnyObjectId nodeObject;
    if(node instanceof FileNode)
      nodeObject = persistFile((FileNode) node, gfs);
    else if(node instanceof DirectoryNode)
      nodeObject = persistDirectory((DirectoryNode) node, isRoot, gfs);
    else
      throw new IllegalStateException();
    node.markClean(nodeObject);
    return nodeObject;
  }

  @Nonnull
  public static AnyObjectId persistRoot(@Nonnull GitFileSystem gfs) throws IOException {
    Node root = gfs.getFileStore().getRoot();
    AnyObjectId ret = persistNode(root, true, gfs);
    assert ret != null;
    return ret;
  }

}
