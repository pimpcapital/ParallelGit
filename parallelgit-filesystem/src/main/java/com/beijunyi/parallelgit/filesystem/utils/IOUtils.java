package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.hierarchy.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.hierarchy.FileNode;
import com.beijunyi.parallelgit.filesystem.hierarchy.Node;
import com.beijunyi.parallelgit.filesystem.io.GitDirectoryStream;
import com.beijunyi.parallelgit.filesystem.io.GitSeekableByteChannel;

public final class IOUtils {

  @Nullable
  private static Node getNode(@Nonnull GitPath path) throws IOException {
    if(!path.isAbsolute())
      throw new IllegalArgumentException(path.toString());
    Node current = path.getFileStore().root();
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
  private static String getFileName(@Nonnull GitPath path) throws IOException {
    GitPath name = path.getFileName();
    if(name == null)
      throw new IllegalArgumentException(path.toString());
    return name.toString();
  }

  @Nonnull
  private static Node forceGetNode(@Nonnull GitPath path) throws IOException {
    Node node = getNode(path);
    if(node == null)
      throw new NoSuchFileException(path.toString());
    return node;
  }

  @Nonnull
  private static FileNode forceGetFile(@Nonnull GitPath path) throws IOException {
    Node node = forceGetNode(path);
    if(node instanceof FileNode)
      return (FileNode) node;
    throw new AccessDeniedException(path.toString());
  }

  @Nonnull
  private static DirectoryNode forceGetDirectory(@Nonnull GitPath path) throws IOException {
    Node node = getNode(path);
    if(node != null && node instanceof DirectoryNode)
      return (DirectoryNode) node;
    throw new NotDirectoryException(path.toString());
  }

  @Nonnull
  public static GitSeekableByteChannel newByteChannel(@Nonnull GitPath path, @Nonnull Set<OpenOption> options, @Nonnull Collection<FileAttribute> attrs) throws IOException {
    if(options.contains(StandardOpenOption.CREATE) || options.contains(StandardOpenOption.CREATE_NEW)) {
      DirectoryNode parent = forceGetDirectory(path.getParent());
      if(options.contains(StandardOpenOption.CREATE_NEW) || parent.getChild(getFileName(path)) == null) {
        parent.addNewFile(getFileName(path), FileAttributeReader.read(attrs).isExecutable());
      }
    }
    return forceGetFile(path).newChannel(options);
  }

  @Nonnull
  public static GitDirectoryStream newDirectoryStream(@Nonnull GitPath path, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    return forceGetDirectory(path).newStream(filter);
  }

  public static void createDirectory(@Nonnull GitPath path) throws IOException {
    if(path.isRoot())
      throw new FileAlreadyExistsException(path.toString());
    DirectoryNode parent = forceGetDirectory(path.getParent());
    String name = getFileName(path);
    parent.addNewDirectory(name);
  }

  public static void copy(@Nonnull GitPath source, @Nonnull GitPath target, @Nonnull Set<CopyOption> options) throws IOException {
    if(source.equals(target))
      return;
    if(target.isRoot())
      throw new FileAlreadyExistsException(target.toString());
    Node sourceNode = forceGetNode(source);
    sourceNode.copyTo(forceGetDirectory(target.getParent()), getFileName(target), options);
  }

  public static void move(@Nonnull GitPath source, @Nonnull GitPath target, @Nonnull Set<CopyOption> options) throws IOException {
    if(source.equals(target))
      return;
    if(target.isRoot())
      throw new FileAlreadyExistsException(target.toString());
    Node sourceNode = forceGetNode(source);
    sourceNode.moveTo(forceGetDirectory(target.getParent()), getFileName(target), options);
  }

  public static void delete(@Nonnull GitPath source) throws IOException {
    forceGetNode(source).delete();
  }

  public static void checkAccess(@Nonnull GitPath path, @Nonnull Set<AccessMode> modes) throws IOException {
    Node node = forceGetNode(path);
    for(AccessMode mode : modes) {
      if(mode == AccessMode.EXECUTE && !node.isExecutableFile())
        throw new AccessDeniedException(path.toString());
    }
  }

  @Nonnull
  public static <V extends FileAttributeView> V getFileAttributeView(@Nonnull GitPath path, @Nonnull Class<V> type) throws IOException, UnsupportedOperationException {
    return forceGetNode(path).getFileAttributeView(type);
  }

}
