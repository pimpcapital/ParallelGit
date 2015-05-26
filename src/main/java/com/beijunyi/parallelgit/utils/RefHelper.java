package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.ParallelGitException;
import org.eclipse.jgit.lib.*;

/**
 * A utility class that provides static helper methods related to {@link Ref} and {@link RefUpdate}.
 */
public final class RefHelper {

  @Nullable
  public static Ref getRef(@Nonnull Repository repo, @Nonnull String name) {
    try {
      return repo.getRef(name);
    } catch(IOException e) {
      throw new ParallelGitException("Could not get ref for " + name, e);
    }
  }

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
  public static Ref getBranchRef(@Nonnull Repository repo, @Nonnull String name) {
    return getRef(repo, getBranchRefName(name));
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
    if(!name.startsWith(Constants.R_HEADS))
      name = Constants.R_HEADS + name;
    if(!Repository.isValidRefName(name))
      throw new ParallelGitException(name + " is not a valid branch ref name");
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


  @Nonnull
  public static RefUpdate updateRef(@Nonnull Repository repo, @Nonnull String ref) {
    try {
      return repo.updateRef(ref);
    } catch(IOException e) {
      throw new ParallelGitException("Could not update ref " + ref, e);
    }
  }

  /**
   * Updates the current ref.
   *
   * This method is the exception friendly version of {@link RefUpdate#update()} which has no checked exception in the
   * method signature. In the case that an {@link IOException} does occur, the source exception can be retrieved from
   * {@link ParallelGitException#getCause()}.
   *
   * @param update a ref update
   * @return the result status of the delete
   */
  @Nonnull
  public static RefUpdate.Result update(@Nonnull RefUpdate update) {
    try {
      return update.update();
    } catch(IOException e) {
      throw new ParallelGitException("Could not update " + update.getName(), e);
    }
  }

  /**
   * Deletes the current ref.
   *
   * This method is the exception friendly version of {@link RefUpdate#delete()} which has no checked exception in the
   * method signature. In the case that an {@link IOException} does occur, the source exception can be retrieved from
   * {@link ParallelGitException#getCause()}.
   *
   * @param update a ref update
   * @return the result status of the delete
   */
  @Nonnull
  public static RefUpdate.Result delete(@Nonnull RefUpdate update) {
    try {
      return update.delete();
    } catch(IOException e) {
      throw new ParallelGitException("Could not delete " + update.getName(), e);
    }
  }

}
