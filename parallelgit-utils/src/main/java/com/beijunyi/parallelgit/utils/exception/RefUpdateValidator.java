package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.RefUpdate;

public final class RefUpdateValidator {

  public static void validate(@Nonnull RefUpdate.Result result) {
    switch(result) {
      case REJECTED_CURRENT_BRANCH:
        throw new RefUpdateRejectedCurrentBranchException(result.name());
      case REJECTED:
        throw new RefUpdateRejectedException(result.name());
      case LOCK_FAILURE:
        throw new RefUpdateLockFailureException(result.name());
      case IO_FAILURE:
        throw new RefUpdateIOFailureException(result.name());
    }
  }

}
