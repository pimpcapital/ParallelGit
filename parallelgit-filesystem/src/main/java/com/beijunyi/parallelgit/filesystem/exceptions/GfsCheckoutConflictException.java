package com.beijunyi.parallelgit.filesystem.exceptions;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.io.GfsCheckoutConflict;

public class GfsCheckoutConflictException extends IllegalStateException {

  private final GfsCheckoutConflict conflict;

  public GfsCheckoutConflictException(GfsCheckoutConflict conflict) {
    this.conflict = conflict;
  }

  @Nonnull
  public GfsCheckoutConflict getConflict() {
    return conflict;
  }

}
