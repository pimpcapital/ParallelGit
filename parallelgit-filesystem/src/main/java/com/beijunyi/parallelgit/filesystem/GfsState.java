package com.beijunyi.parallelgit.filesystem;

public enum GfsState {
  NORMAL,
  COMMITTING,
  MERGING,
  CHERRY_PICKING,
  REBASING
}
