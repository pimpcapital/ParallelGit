package com.beijunyi.parallelgit.filesystem.utils;

import javax.annotation.Nonnull;

public class GfsPathUtils {

  @Nonnull
  public static String toAbsolutePath(String path) {
    return path.startsWith("/") ? path : ("/" + path);
  }

  @Nonnull
  public static String addTrailingSlash(String path) {
    return path.endsWith("/") ? path : (path + "/");
  }

  public static boolean isRoot(String path) {
    return "/".equals(path);
  }


}
