package com.beijunyi.parallelgit.utils.exceptions;

import javax.annotation.Nonnull;

public class RefUpdateRejectedCurrentBranchException extends RefUpdateRejectedException {

  public RefUpdateRejectedCurrentBranchException(@Nonnull String message) {
    super(message);
  }

}
