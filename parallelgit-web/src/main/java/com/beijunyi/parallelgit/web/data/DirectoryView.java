package com.beijunyi.parallelgit.web.data;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class DirectoryView {

  private final FileAttributes attributes;
  private final List<FileAttributes> children;

  public DirectoryView(@Nonnull FileAttributes attributes, @Nonnull List<FileAttributes> children) {
    this.attributes = attributes;
    this.children = children;
  }

  @Nonnull
  public static DirectoryView open(@Nonnull Path path) throws IOException{
    FileAttributes attributes = FileAttributes.read(path);
    List<FileAttributes> children = new ArrayList<>();
    try(DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
      for(Path child : stream) {
        children.add(FileAttributes.read(child));
      }
    }
    Collections.sort(children);
    return new DirectoryView(attributes, children);
  }

  @Nonnull
  public FileAttributes getAttributes() {
    return attributes;
  }

  @Nonnull
  public List<FileAttributes> getChildren() {
    return children;
  }

}
