package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.utils.FileAttributeReader;

import static com.beijunyi.parallelgit.filesystem.io.FileNode.newFile;
import static java.nio.file.AccessMode.EXECUTE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;

public final class GfsIO {

  @Nonnull
  private static GitPath getParent(GitPath child) {
    if(child.isRoot()) throw new IllegalArgumentException(child.toString());
    GitPath parent = child.getParent();
    if(parent == null) throw new IllegalStateException(child.toString());
    return parent;
  }

  @Nullable
  private static Node findNode(GitPath path) throws IOException {
    if(!path.isAbsolute()) throw new IllegalArgumentException(path.toString());
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
  private static Node getNode(GitPath path) throws IOException {
    Node node = findNode(path);
    if(node == null) throw new NoSuchFileException(path.toString());
    return node;
  }

  @Nonnull
  private static FileNode asFile(@Nullable Node node, GitPath path) throws NoSuchFileException, AccessDeniedException {
    if(node == null) throw new NoSuchFileException(path.toString());
    if(node instanceof FileNode) return (FileNode) node;
    throw new AccessDeniedException(path.toString());
  }

  @Nonnull
  static FileNode findFile(GitPath file) throws IOException {
    return asFile(findNode(file), file);
  }

  @Nonnull
  private static DirectoryNode asDirectory(@Nullable Node node, GitPath path) throws NotDirectoryException {
    if(node instanceof DirectoryNode) return (DirectoryNode) node;
    throw new NotDirectoryException(path.toString());
  }

  @Nonnull
  private static DirectoryNode findDirectory(GitPath dir) throws IOException {
    return asDirectory(findNode(dir), dir);
  }

  @Nonnull
  private static String getFileName(GitPath path) throws IOException {
    GitPath name = path.getFileName();
    assert name != null;
    return name.toString();
  }

  @Nonnull
  public static SeekableByteChannel newByteChannel(GitPath file, Set<? extends OpenOption> options, Collection<? extends FileAttribute> attrs) throws IOException {
    if(file.isRoot()) throw new AccessDeniedException(file.toString());
    FileNode node;
    if(options.contains(CREATE) || options.contains(CREATE_NEW)) {
      DirectoryNode parent = findDirectory(getParent(file));
      String name = getFileName(file);
      if(options.contains(CREATE_NEW) || !parent.hasChild(name)) {
        node = newFile(FileAttributeReader.read(attrs).isExecutable(), parent);
        if(!parent.addChild(name, node, false)) throw new FileAlreadyExistsException(file.toString());
      } else {
        node = asFile(parent.getChild(name), file);
      }
    } else {
      node = findFile(file);
    }
    if (options.contains(WRITE)) {
      return new GfsSeekableByteChannel(node, options);
    } else {
      return new GfsSeekableReadOnlyByteChannel(node, options);
    }
  }

  @Nonnull
  public static GfsDirectoryStream newDirectoryStream(GitPath dir, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    return new GfsDirectoryStream(findDirectory(dir), dir, filter);
  }

  public static void createDirectory(GitPath dir) throws IOException {
    if(dir.isRoot()) throw new FileAlreadyExistsException(dir.toString());
    DirectoryNode parent = findDirectory(getParent(dir));
    if(!parent.addChild(getFileName(dir), DirectoryNode.newDirectory(parent), false))
      throw new FileAlreadyExistsException(dir.toString());
  }

  public static boolean copy(GitPath source, GitPath target, Set<CopyOption> options) throws IOException {
    Node sourceNode = getNode(source);
    if(source.equals(target)) return false;
    if(target.isRoot()) throw new AccessDeniedException(target.toString());
    GitPath targetParent = getParent(target);
    DirectoryNode targetDirectory = findDirectory(targetParent);
    Node targetNode = sourceNode.clone(targetDirectory);
    if(!targetDirectory.addChild(getFileName(target), targetNode, options.contains(REPLACE_EXISTING)))
      throw new FileAlreadyExistsException(target.toString());
    return true;
  }

  public static boolean move(GitPath source, GitPath target, Set<CopyOption> options) throws IOException {
    if(copy(source, target, options)) {
      delete(source);
      return true;
    }
    return false;
  }

  public static void delete(GitPath file) throws IOException {
    if(file.isRoot()) throw new AccessDeniedException(file.toString());
    GitPath parentPath = getParent(file);
    Node parent = findNode(getParent(file));
    if(parent == null || !parent.isDirectory() || !asDirectory(parent, parentPath).removeChild(getFileName(file)))
      throw new NoSuchFileException(file.toString());
  }

  public static void checkAccess(GitPath path, Set<AccessMode> modes) throws IOException {
    Node node = getNode(path);
    if(modes.contains(EXECUTE) && !node.isExecutableFile()) throw new AccessDeniedException(path.toString());
  }

  @Nullable
  public static <V extends FileAttributeView> V getFileAttributeView(GitPath path, Class<V> type) throws IOException, UnsupportedOperationException {
    Node node = findNode(path);
    return node != null ? GfsFileAttributeView.forNode(node, type) : null;
  }

}
