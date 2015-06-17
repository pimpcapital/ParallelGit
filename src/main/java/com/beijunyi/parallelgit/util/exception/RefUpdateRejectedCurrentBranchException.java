package com.beijunyi.parallelgit.util.exception;

import javax.annotation.Nonnull;

public class RefUpdateRejectedCurrentBranchException extends RefUpdateRejectedException {

  public RefUpdateRejectedCurrentBranchException(@Nonnull String message) {
    super(message);
  }

}
