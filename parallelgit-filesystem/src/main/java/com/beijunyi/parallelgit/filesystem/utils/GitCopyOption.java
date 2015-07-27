package com.beijunyi.parallelgit.filesystem.utils;

import java.nio.file.CopyOption;

public enum GitCopyOption implements CopyOption {
  CLONE,
  LOAD_BEFORE_COPY,
  MARK_DIRTY,
  UNLOAD_AFTER_COPY,
}
