package com.beijunyi.parallelgit.utils.exception;

import javax.annotation.Nonnull;

public class RefUpdateRejectedCurrentBranchException extends RefUpdateRejectedException {

  public RefUpdateRejectedCurrentBranchException(@Nonnull String message) {
    super(message);
  }

}
