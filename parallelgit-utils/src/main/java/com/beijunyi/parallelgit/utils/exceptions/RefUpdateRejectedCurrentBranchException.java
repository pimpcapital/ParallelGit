package com.beijunyi.parallelgit.utils.exceptions;

public class RefUpdateRejectedCurrentBranchException extends RefUpdateRejectedException {

  public RefUpdateRejectedCurrentBranchException(String message) {
    super(message);
  }

}
