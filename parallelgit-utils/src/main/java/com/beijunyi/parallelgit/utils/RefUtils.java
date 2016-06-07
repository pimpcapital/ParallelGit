package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;

public final class RefUtils {

  private static boolean matchesPrefix(String name, String prefix) {
    return name.startsWith(prefix);
  }

  public static boolean matchesRefPrefix(String name) {
    return matchesPrefix(name, Constants.R_REFS);
  }

  public static boolean matchesBranchRefPrefix(String name) {
    return matchesPrefix(name, Constants.R_HEADS);
  }

  public static boolean matchesTagRefPrefix(String name) {
    return matchesPrefix(name, Constants.R_TAGS);
  }

  public static boolean isBranchRef(Ref name) {
    return matchesBranchRefPrefix(name.getName());
  }

  public static boolean isTagRef(Ref name) {
    return matchesTagRefPrefix(name.getName());
  }

  @Nonnull
  public static String ensureRefPrefix(String name, String prefix) {
    if(!matchesPrefix(name, prefix)) {
      if(matchesRefPrefix(name))
        throw new IllegalArgumentException("\"" + prefix + "\" is not the prefix of " + name);
      name = prefix + name;
    }
    if(!Repository.isValidRefName(name))
      throw new IllegalArgumentException(name + " is not a valid ref name");
    return name;
  }

  @Nonnull
  public static String ensureBranchRefName(String name) {
    return ensureRefPrefix(name, Constants.R_HEADS);
  }

  @Nonnull
  public static String ensureTagRefName(String name) {
    return ensureRefPrefix(name, Constants.R_TAGS);
  }

  @Nullable
  public static Ref getBranchRef(String name, Repository repo) throws IOException {
    return repo.getRef(ensureBranchRefName(name));
  }

  @Nullable
  public static Ref getTagRef(String name, Repository repo) throws IOException {
    return repo.getRef(ensureTagRefName(name));
  }

  @Nonnull
  public static List<ReflogEntry> getRefLogs(String ref, int max, Repository repository) throws IOException {
    ReflogReader reader = repository.getReflogReader(ref);
    return reader != null ? reader.getReverseEntries(max) : Collections.<ReflogEntry>emptyList();
  }


}
