package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.RefUpdate;

public final class RefUpdateValidator {

  public static void validate(@Nonnull String name, @Nonnull RefUpdate.Result result) {
    switch(result) {
      case REJECTED_CURRENT_BRANCH:
        throw new RefUpdateRejectedCurrentBranchException(name);
      case REJECTED:
        throw new RefUpdateRejectedException(name);
      case LOCK_FAILURE:
        throw new RefUpdateLockFailureException(name);
      case IO_FAILURE:
        throw new RefUpdateIOFailureException(name);
    }
  }

}
