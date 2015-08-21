package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;

public final class RefHelper {

  public static boolean matchesRefPrefix(@Nonnull String ref) {
    return ref.startsWith(Constants.R_REFS);
  }

  public static boolean matchesBranchRefPrefix(@Nonnull String ref) {
    return ref.startsWith(Constants.R_HEADS);
  }

  public static boolean matchesTagRefPrefix(@Nonnull String ref) {
    return ref.startsWith(Constants.R_TAGS);
  }

  public static boolean isBranchRef(@Nonnull Ref ref) {
    return matchesBranchRefPrefix(ref.getName());
  }

  public static boolean isTagRef(@Nonnull Ref ref) {
    return matchesTagRefPrefix(ref.getName());
  }

  @Nonnull
  public static String getBranchRefName(@Nonnull String name) {
    if(!matchesBranchRefPrefix(name)) {
      if(matchesRefPrefix(name))
        throw new IllegalArgumentException(name + " is not a branch ref");
      name = Constants.R_HEADS + name;
    }
    if(!Repository.isValidRefName(name))
      throw new IllegalArgumentException(name + " is not a valid branch ref");
    return name;
  }

  @Nullable
  public static Ref getBranchRef(@Nonnull Repository repo, @Nonnull String name) throws IOException {
    return repo.getRef(getBranchRefName(name));
  }

  @Nonnull
  public static List<ReflogEntry> getReflogs(@Nonnull Repository repository, @Nonnull String branch, int max) throws IOException {
    ReflogReader reader = repository.getReflogReader(getBranchRefName(branch));
    return reader.getReverseEntries(max);
  }

  @Nonnull
  public static List<ReflogEntry> getReflogs(@Nonnull Repository repository, @Nonnull String branch) throws IOException {
    return getReflogs(repository, branch, Integer.MAX_VALUE);
  }

  @Nullable
  public static ReflogEntry getLastReflog(@Nonnull Repository repository, @Nonnull String branch) throws IOException {
    List<ReflogEntry> entries = getReflogs(repository, branch, 1);
    if(entries.isEmpty())
      return null;
    return entries.get(0);
  }

  @Nonnull
  public static List<String> getReflogComments(@Nonnull Repository repository, @Nonnull String branch, int max) throws IOException {
    List<String> ret = new ArrayList<>();
    for(ReflogEntry entry : getReflogs(repository, branch, max))
      ret.add(entry.getComment());
    return ret;
  }

  @Nonnull
  public static List<String> getReflogComments(@Nonnull Repository repository, @Nonnull String branch) throws IOException {
    return getReflogComments(repository, branch, Integer.MAX_VALUE);
  }

  @Nullable
  public static String getLastReflogComment(@Nonnull Repository repository, @Nonnull String branch) throws IOException {
    ReflogEntry entry = getLastReflog(repository, branch);
    if(entry == null)
      return null;
    return entry.getComment();
  }


}
