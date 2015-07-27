package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.filesystem.hierarchy.DirectoryNode;
import com.beijunyi.parallelgit.filesystem.hierarchy.Node;
import com.beijunyi.parallelgit.filesystem.io.GitDirectoryStream;

public final class IOUtils {

  @Nonnull
  public static String getFileName(@Nonnull GitPath path) throws IOException {
    GitPath name = path.getFileName();
    if(name == null)
      throw new IllegalArgumentException(path.toString());
    return name.toString();
  }

  @Nonnull
  public static GitDirectoryStream newDirectoryStream(@Nonnull GitPath path, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    return path.getNode().asDirectory().newStream(filter);
  }


  public static void createDirectory(@Nonnull GitPath path) throws IOException {
    DirectoryNode parent = path.getParentNode();
    String name = getFileName(path);
    parent.addChild(name, DirectoryNode.newDirectory(), Collections.<CopyOption>emptySet());
  }

  public static void copy(@Nonnull GitPath source, @Nonnull GitPath target, @Nonnull Set<CopyOption> options) throws IOException {
    if(source.equals(target))
      return;
    Node sourceNode = source.getNode();
    sourceNode.copyTo(target.getParentNode(), getFileName(target), options);
  }

  public static void move(@Nonnull GitPath source, @Nonnull GitPath target, @Nonnull Set<CopyOption> options) throws IOException {
    if(source.equals(target))
      return;
    Node sourceNode = source.getNode();
    sourceNode.moveTo(target.getParentNode(), getFileName(target), options);
  }

  public static void delete(@Nonnull GitPath source) throws IOException {
    source.getNode().delete();
  }

}
