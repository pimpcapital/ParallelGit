package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;

public final class RefUtils {

  private static boolean matchesPrefix(@Nonnull String name, @Nonnull String prefix) {
    return name.startsWith(prefix);
  }

  public static boolean matchesRefPrefix(@Nonnull String name) {
    return matchesPrefix(name, Constants.R_REFS);
  }

  public static boolean matchesBranchRefPrefix(@Nonnull String name) {
    return matchesPrefix(name, Constants.R_HEADS);
  }

  public static boolean matchesTagRefPrefix(@Nonnull String name) {
    return matchesPrefix(name, Constants.R_TAGS);
  }

  public static boolean isBranchRef(@Nonnull Ref name) {
    return matchesBranchRefPrefix(name.getName());
  }

  public static boolean isTagRef(@Nonnull Ref name) {
    return matchesTagRefPrefix(name.getName());
  }

  @Nonnull
  public static String ensureRefPrefix(@Nonnull String name, @Nonnull String prefix) {
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
  public static String ensureBranchRefName(@Nonnull String name) {
    return ensureRefPrefix(name, Constants.R_HEADS);
  }

  @Nonnull
  public static String ensureTagRefName(@Nonnull String name) {
    return ensureRefPrefix(name, Constants.R_TAGS);
  }

  @Nullable
  public static Ref getBranchRef(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    return repo.getRef(ensureBranchRefName(name));
  }

  @Nonnull
  public static List<ReflogEntry> getRefLogs(@Nonnull String branch, int max, @Nonnull Repository repository) throws IOException {
    ReflogReader reader = repository.getReflogReader(ensureBranchRefName(branch));
    return reader.getReverseEntries(max);
  }

  @Nonnull
  public static List<ReflogEntry> getRefLogs(@Nonnull String branch, @Nonnull Repository repository) throws IOException {
    return getRefLogs(branch, Integer.MAX_VALUE, repository);
  }

  @Nullable
  public static ReflogEntry getLastRefLog(@Nonnull String branch, @Nonnull Repository repository) throws IOException {
    List<ReflogEntry> entries = getRefLogs(branch, 1, repository);
    if(entries.isEmpty())
      return null;
    return entries.get(0);
  }

  @Nonnull
  public static List<String> getRefLogComments(@Nonnull String branch, int max, @Nonnull Repository repository) throws IOException {
    List<String> ret = new ArrayList<>();
    for(ReflogEntry entry : getRefLogs(branch, max, repository))
      ret.add(entry.getComment());
    return ret;
  }

  @Nonnull
  public static List<String> getRefLogComments(@Nonnull String branch, @Nonnull Repository repository) throws IOException {
    return getRefLogComments(branch, Integer.MAX_VALUE, repository);
  }

  @Nullable
  public static String getLastRefLogComment(@Nonnull String branch, @Nonnull Repository repository) throws IOException {
    ReflogEntry entry = getLastRefLog(branch, repository);
    if(entry == null)
      return null;
    return entry.getComment();
  }


}
