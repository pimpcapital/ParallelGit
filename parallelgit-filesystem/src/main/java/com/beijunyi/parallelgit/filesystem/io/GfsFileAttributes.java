package com.beijunyi.parallelgit.filesystem.io;

import java.nio.file.attribute.*;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Basic attributes associated with a file in a git file system.
 */
public abstract class GfsFileAttributes {

  public static class Basic implements BasicFileAttributes {
    private final FileTime lastModifiedTime;
    private final FileTime lastAccessTime;
    private final FileTime creationTime;
    private final boolean isRegularFile;
    private final boolean isDirectory;
    private final boolean isSymbolicLink;
    private final boolean isOther;
    private final long size;
    private final Object fileKey;

    Basic(@Nonnull Map<String, Object> attributes) {
      lastModifiedTime = (FileTime) attributes.get(GfsFileAttributeView.Basic.LAST_MODIFIED_TIME_NAME);
      lastAccessTime = (FileTime) attributes.get(GfsFileAttributeView.Basic.LAST_ACCESS_TIME_NAME);
      creationTime = (FileTime) attributes.get(GfsFileAttributeView.Basic.CREATION_TIME_NAME);
      isRegularFile = (boolean) attributes.get(GfsFileAttributeView.Basic.IS_REGULAR_FILE_NAME);
      isDirectory = (boolean) attributes.get(GfsFileAttributeView.Basic.IS_DIRECTORY_NAME);
      isSymbolicLink = (boolean) attributes.get(GfsFileAttributeView.Basic.IS_SYMBOLIC_LINK_NAME);
      isOther = (boolean) attributes.get(GfsFileAttributeView.Basic.IS_OTHER_NAME);
      size = (long) attributes.get(GfsFileAttributeView.Basic.SIZE_NAME);
      fileKey = attributes.get(GfsFileAttributeView.Basic.FILE_KEY_NAME);
    }

    @Nonnull
    @Override
    public FileTime lastModifiedTime() {
      return lastModifiedTime;
    }

    @Nonnull
    @Override
    public FileTime lastAccessTime() {
      return lastAccessTime;
    }

    @Nonnull
    @Override
    public FileTime creationTime() {
      return creationTime;
    }

    @Override
    public boolean isRegularFile() {
      return isRegularFile;
    }

    /**
     * Tells if the file is a directory.
     *
     * @return  {@code true} if the file is a directory
     */
    @Override
    public boolean isDirectory() {
      return isDirectory;
    }

    /**
     * Returns {@code false} as symbolic link is not supported.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isSymbolicLink() {
      return isSymbolicLink;
    }

    /**
     * Returns {@code false} as other types of files are not supported.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isOther() {
      return isOther;
    }

    /**
     * Returns the size of the file in bytes.
     *
     * @return  the file size in bytes
     */
    @Override
    public long size() {
      return size;
    }

    /**
     * Returns {@code null} as file key is not supported.
     *
     * @return  {@code null}
     */
    @Nullable
    @Override
    public Object fileKey() {
      return fileKey;
    }
  }

  public static class Posix extends Basic implements PosixFileAttributes {

    private final UserPrincipal owner;
    private final GroupPrincipal group;
    private final Set<PosixFilePermission> permissions;

    @SuppressWarnings("unchecked")
    public Posix(@Nonnull Map<String, Object> attributes) {
      super(attributes);
      owner = (UserPrincipal) attributes.get(GfsFileAttributeView.Posix.OWNER_NAME);
      group = (GroupPrincipal) attributes.get(GfsFileAttributeView.Posix.GROUP_NAME);
      permissions = (Set<PosixFilePermission>) attributes.get(GfsFileAttributeView.Posix.PERMISSIONS_NAME);
    }

    @Override
    public UserPrincipal owner() {
      return owner;
    }

    @Override
    public GroupPrincipal group() {
      return group;
    }

    @Override
    public Set<PosixFilePermission> permissions() {
      return permissions;
    }

  }

}
