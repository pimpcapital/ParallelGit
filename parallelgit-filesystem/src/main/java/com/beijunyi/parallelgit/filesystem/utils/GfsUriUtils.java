package com.beijunyi.parallelgit.filesystem.utils;

import java.net.URI;
import java.nio.file.ProviderMismatchException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystemProvider;

public final class GfsUriUtils {

  public final static String SID_KEY = "sid";

  static void checkScheme(URI uri) throws ProviderMismatchException {
    if(!GitFileSystemProvider.GFS.equalsIgnoreCase(uri.getScheme()))
      throw new ProviderMismatchException(uri.getScheme());
  }

  @Nonnull
  public static String getRepository(URI uri) {
    checkScheme(uri);
    String path = uri.getPath();
    if(path.length() > 1 && path.endsWith("/") && !path.endsWith(":/"))
      path = path.substring(0, path.length() - 1);
    return path;
  }

  @Nonnull
  public static String getFile(URI uri) throws ProviderMismatchException {
    checkScheme(uri);
    String fragment = uri.getFragment();
    if(fragment == null)
      fragment = "";
    if(!fragment.startsWith("/"))
      fragment = "/" + fragment;
    if(fragment.length() > 1 && fragment.endsWith("/"))
      fragment = fragment.substring(0, fragment.length() - 1);
    return fragment;
  }

  @Nonnull
  public static Map<String, String> parseQuery(@Nullable String query, @Nullable Set<String> keys) {
    Map<String, String> params = new HashMap<>();
    if(query != null) {
      String[] pairs = query.split("&");
      int count = 0;
      for(String pair : pairs) {
        String[] keyValue = pair.split("=", 2);
        String key = keyValue[0];
        if(keys == null || keys.contains(key)) {
          params.put(key, keyValue.length > 1 ? keyValue[1] : null);
          if(keys != null && ++count == keys.size())
            break;
        }
      }
    }
    return params;
  }

  @Nullable
  public static String getSession(URI uri) throws ProviderMismatchException {
    checkScheme(uri);
    return parseQuery(uri.getQuery(), Collections.singleton(SID_KEY)).get(SID_KEY);
  }

}
