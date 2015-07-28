package com.beijunyi.parallelgit.filesystem.utils;

import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import javax.annotation.Nonnull;

public class FileAttributeReader {

  private final Map<String, Object> attributeMap = new HashMap<>();

  private FileAttributeReader(@Nonnull Collection<FileAttribute> attributes) {
    for(FileAttribute attribute : attributes)
      attributeMap.put(attribute.name(), attribute.value());
  }

  @Nonnull
  public static FileAttributeReader read(@Nonnull Collection<FileAttribute> attributes) {
    return new FileAttributeReader(attributes);
  }

  @SuppressWarnings("unchecked")
  public boolean isExecutable() {
    Set<PosixFilePermission> permissions = (Set<PosixFilePermission>) attributeMap.get("posix:permissions");
    return permissions != null && permissions.contains(PosixFilePermission.OWNER_EXECUTE);
  }
}
