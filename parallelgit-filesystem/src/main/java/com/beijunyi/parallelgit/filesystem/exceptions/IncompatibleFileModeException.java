package com.beijunyi.parallelgit.filesystem.exceptions;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.FileMode;

public class IncompatibleFileModeException extends IllegalArgumentException {

  private final FileMode current;
  private final FileMode proposed;

  public IncompatibleFileModeException(FileMode current, FileMode proposed) {
    super(current.toString() + " -> " + proposed.toString());
    this.current = current;
    this.proposed = proposed;
  }

  @Nonnull
  public FileMode getCurrent() {
    return current;
  }

  @Nonnull
  public FileMode getProposed() {
    return proposed;
  }

}
