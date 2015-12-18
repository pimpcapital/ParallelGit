package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.utils.FileAttributeReader;

import static java.nio.file.AccessMode.EXECUTE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;

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
        current = ((DirectoryNode) current).getChild(name.toString());
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
  private static DirectoryNode findDirectory(@Nonnull GitPath dir) throws IOException {
    return asDirectory(findNode(dir), dir);
  }

  @Nonnull
  private static String getFileName(@Nonnull GitPath path) throws IOException {
    GitPath name = path.getFileName();
    assert name != null;
    return name.toString();
  }

  @Nonnull
  public static GfsSeekableByteChannel newByteChannel(@Nonnull GitPath file, @Nonnull Set<? extends OpenOption> options, @Nonnull Collection<? extends FileAttribute> attrs) throws IOException {
    if(file.isRoot())
      throw new AccessDeniedException(file.toString());
    FileNode node;
    if(options.contains(CREATE) || options.contains(CREATE_NEW)) {
      DirectoryNode parent = findDirectory(getParent(file));
      String name = getFileName(file);
      if(options.contains(CREATE_NEW) || !parent.hasChild(name)) {
        node = FileNode.newFile(FileAttributeReader.read(attrs).isExecutable(), parent.getDataService());
        if(!parent.addChild(name, node, false))
          throw new FileAlreadyExistsException(file.toString());
      } else {
        node = asFile(parent.getChild(name), file);
      }
    } else
      node = findFile(file);
    return new GfsSeekableByteChannel(node, options);
  }

  @Nonnull
  public static GfsDirectoryStream newDirectoryStream(@Nonnull GitPath dir, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    return new GfsDirectoryStream(findDirectory(dir), dir, filter);
  }

  public static void createDirectory(@Nonnull GitPath dir) throws IOException {
    if(dir.isRoot())
      throw new FileAlreadyExistsException(dir.toString());
    DirectoryNode parent = findDirectory(getParent(dir));
    if(!parent.addChild(getFileName(dir), DirectoryNode.newDirectory(parent.getDataService()), false))
      throw new FileAlreadyExistsException(dir.toString());
  }

  public static boolean copy(@Nonnull GitPath source, @Nonnull GitPath target, @Nonnull Set<CopyOption> options) throws IOException {
    Node sourceNode = getNode(source);
    if(source.equals(target))
      return false;
    if(target.isRoot())
      throw new AccessDeniedException(target.toString());
    GitPath targetParent = getParent(target);
    DirectoryNode targetDirectory = findDirectory(targetParent);
    Node targetNode = sourceNode.clone(getDataService(target));
    if(!targetDirectory.addChild(getFileName(target), targetNode, options.contains(REPLACE_EXISTING)))
      throw new FileAlreadyExistsException(target.toString());
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
    DirectoryNode parent = findDirectory(getParent(file));
    if(!parent.removeChild(getFileName(file)))
      throw new NoSuchFileException(file.toString());
  }

  public static void checkAccess(@Nonnull GitPath path, @Nonnull Set<AccessMode> modes) throws IOException {
    Node node = getNode(path);
    if(modes.contains(EXECUTE) && !node.isExecutableFile())
      throw new AccessDeniedException(path.toString());
  }

  @Nullable
  public static <V extends FileAttributeView> V getFileAttributeView(@Nonnull GitPath path, @Nonnull Class<V> type) throws IOException, UnsupportedOperationException {
    Node node = findNode(path);
    if(node != null)
      return GfsFileAttributeView.forNode(node, type);
    return null;
  }

  @Nonnull
  private static GfsObjectService getDataService(@Nonnull GitPath path) {
    return path.getFileSystem().getObjectService();
  }

}
