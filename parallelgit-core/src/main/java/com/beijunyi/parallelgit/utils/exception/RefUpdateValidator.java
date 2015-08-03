package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.RefUpdate;

public final class RefUpdateValidator {

  public static void validate(@Nonnull String ref, @Nonnull RefUpdate.Result result) {
    switch(result) {
      case REJECTED_CURRENT_BRANCH:
        throw new RefUpdateRejectedCurrentBranchException(ref);
      case REJECTED:
        throw new RefUpdateRejectedException(ref);
      case LOCK_FAILURE:
        throw new RefUpdateLockFailureException(ref);
      case IO_FAILURE:
        throw new RefUpdateIOFailureException(ref);
    }
  }

}
