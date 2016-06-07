package com.beijunyi.parallelgit.filesystem.test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RecursiveDelete<P extends Path> extends SimpleFileVisitor<P> {

  @Nonnull
  @Override
  public FileVisitResult postVisitDirectory(P dir, @Nullable IOException exc) throws IOException {
    try {
      Files.delete(dir);
    } catch(IOException e) {
      if(!dir.equals(dir.getRoot()))
        throw e;
    }
    return FileVisitResult.CONTINUE;
  }

  @Nonnull
  @Override
  public FileVisitResult visitFile(P file, BasicFileAttributes attrs) throws IOException {
    Files.delete(file);
    return FileVisitResult.CONTINUE;
  }

}
