package com.beijunyi.parallelgit.filesystem;

public enum GfsState {
  NORMAL,
  CHECKING_OUT,
  COMMITTING,
  MERGING,
  MERGING_CONFLICT,
  CHERRY_PICKING,
  REBASING
}
