package com.beijunyi.parallelgit.filesystem;

public enum GfsState {
  NORMAL,
  COMMITTING,
  MERGING,
  MERGING_CONFLICT,
  CHERRY_PICKING,
  REBASING
}
