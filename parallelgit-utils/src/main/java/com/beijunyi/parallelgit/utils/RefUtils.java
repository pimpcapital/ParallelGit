package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import org.eclipse.jgit.lib.*;

import static org.eclipse.jgit.lib.Constants.*;

public final class RefUtils {

  private static boolean matchesPrefix(String name, String prefix) {
    return name.startsWith(prefix);
  }

  public static boolean matchesRefPrefix(String name) {
    return matchesPrefix(name, R_REFS);
  }

  public static boolean matchesBranchRefPrefix(String name) {
    return matchesPrefix(name, R_HEADS);
  }

  public static boolean matchesTagRefPrefix(String name) {
    return matchesPrefix(name, R_TAGS);
  }

  public static boolean isBranchRef(Ref name) {
    return matchesBranchRefPrefix(name.getName());
  }

  public static boolean isTagRef(Ref name) {
    return matchesTagRefPrefix(name.getName());
  }

  @Nonnull
  public static String appendPrefix(String name, String prefix) {
    if(!matchesPrefix(name, prefix)) {
      if(matchesRefPrefix(name)) throw new IllegalArgumentException("\"" + prefix + "\" is not the prefix of " + name);
      name = prefix + name;
    }
    if(!Repository.isValidRefName(name)) throw new IllegalArgumentException(name + " is not a valid ref name");
    return name;
  }

  @Nonnull
  public static String fullBranchName(String name) {
    return appendPrefix(name, R_HEADS);
  }

  @Nonnull
  public static String fullTagRef(String name) {
    return appendPrefix(name, R_TAGS);
  }

  @Nonnull
  public static Ref getBranchRef(String name, Repository repo) throws IOException {
    Ref ret = repo.exactRef(fullBranchName(name));
    if(ret == null) throw new NoSuchBranchException(name);
    return ret;
  }

  @Nullable
  public static Ref getTagRef(String name, Repository repo) throws IOException {
    return repo.exactRef(fullTagRef(name));
  }

  @Nonnull
  public static List<ReflogEntry> getRefLogs(String ref, int max, Repository repository) throws IOException {
    ReflogReader reader = repository.getReflogReader(ref);
    return reader != null ? reader.getReverseEntries(max) : Collections.<ReflogEntry>emptyList();
  }


}
