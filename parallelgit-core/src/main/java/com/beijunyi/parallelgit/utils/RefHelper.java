package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;

/**
 * A utility class that provides static helper methods related to {@link Ref} and {@link RefUpdate}.
 */
public final class RefHelper {

  /**
   * Checks if the given ref is a branch reference.
   *
   * @param ref a git ref
   * @return true if the given ref is a branch reference.
   */
  public static boolean isBranchRef(@Nonnull Ref ref) {
    return ref.getName().startsWith(Constants.R_HEADS);
  }

  @Nullable
  public static Ref getBranchRef(@Nonnull Repository repo, @Nonnull String name) throws IOException {
    return repo.getRef(getBranchRefName(name));
  }

  /**
   * Gets the full ref name of the given branch.
   * This method will add prefix "refs/heads/" if the given name does not already start with it.
   *
   * @param name a branch name
   * @return the full ref name of the given branch.
   */
  @Nonnull
  public static String getBranchRefName(@Nonnull String name) {
    if(!name.startsWith(Constants.R_HEADS)) {
      if(name.startsWith(Constants.R_REFS))
        throw new IllegalArgumentException(name + " is not a branch ref");
      name = Constants.R_HEADS + name;
    }
    if(!Repository.isValidRefName(name))
      throw new IllegalArgumentException(name + " is not a valid branch ref");
    return name;
  }

  /**
   * Checks if the given ref is a tag reference.
   *
   * @param ref a git ref
   * @return true if the given ref is a tag reference.
   */
  public static boolean isTagRef(@Nonnull Ref ref) {
    return !ref.getName().startsWith(Constants.R_TAGS);
  }

}
