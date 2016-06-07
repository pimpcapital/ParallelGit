package com.beijunyi.parallelgit.filesystem.exceptions;

import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsState;

public class BadGfsStateException extends IllegalStateException {

  private final GfsState state;

  public BadGfsStateException(GfsState state) {
    this.state = state;
  }

  @Nonnull
  public GfsState getState() {
    return state;
  }
}
