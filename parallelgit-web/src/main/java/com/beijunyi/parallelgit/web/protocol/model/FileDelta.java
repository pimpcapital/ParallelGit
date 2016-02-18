package com.beijunyi.parallelgit.web.protocol.model;

import javax.annotation.Nonnull;

public class FileDelta {

  private final String name;
  private final FileState current;
  private final FileState previous;
  private final DeltaType type;

  public FileDelta(@Nonnull String name, @Nonnull FileState current, @Nonnull FileState previous, @Nonnull DeltaType type) {
    this.name = name;
    this.current = current;
    this.previous = previous;
    this.type = type;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public FileState getCurrent() {
    return current;
  }

  @Nonnull
  public FileState getPrevious() {
    return previous;
  }

  @Nonnull
  public DeltaType getType() {
    return type;
  }

}
