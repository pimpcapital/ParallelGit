package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.ParallelGitException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;

public final class RevWalkHelper {

  /**
   * Marks the start of the {@link RevWalk} with the given {@link RevCommit}.
   *
   * This method is the exception friendly version of {@link RevWalk#markStart(RevCommit)} which has no checked
   * exception in the method signature. In the case that an {@link IOException} does occur, the source exception can be
   * retrieved from {@link ParallelGitException#getCause()}.
   *
   * @param revWalk a rev walk
   * @param start a rev commit
   */
  public static void markStart(@Nonnull RevWalk revWalk, @Nonnull RevCommit start) {
    try {
      revWalk.markStart(start);
    } catch(IOException e) {
      throw new ParallelGitException("Could not mark the start of rev walk with " + start.getName(), e);
    }
  }

  /**
   * Peels the given object id and finds the first non-tag {@link RevObject} it references.
   *
   * This method is the exception friendly version of {@link RevWalk#peel(RevObject)} which has no checked exception in
   * the method signature. In the case that an {@link IOException} does occur, the source exception can be retrieved
   * from {@link ParallelGitException#getCause()}.
   *
   * @param revWalk a rev walk
   * @param objectId an object id
   * @return the first non-tag object that the given object id references
   */
  @Nonnull
  public static RevObject peel(@Nonnull RevWalk revWalk, @Nonnull AnyObjectId objectId) {
    try {
      return revWalk.peel(revWalk.parseAny(objectId));
    } catch(IOException e) {
      throw new ParallelGitException("Could not peel " + objectId.getName(), e);
    }
  }
}
