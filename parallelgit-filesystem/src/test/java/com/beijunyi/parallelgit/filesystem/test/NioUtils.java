package com.beijunyi.parallelgit.filesystem.test;

import java.nio.file.FileVisitor;
import java.nio.file.Path;

public class NioUtils {

  public static final FileVisitor<Path> RECURSIVE_DELETE = new RecursiveDelete<>();

}
