package com.beijunyi.parallelgit.filesystem;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Basic attributes associated with a file in a git file system.
 */
public class GitFileAttributes implements BasicFileAttributes {

  private final FileTime lastModifiedTime;
  private final FileTime lastAccessTime;
  private final FileTime creationTime;
  private final boolean isRegularFile;
  private final boolean isDirectory;
  private final boolean isSymbolicLink;
  private final boolean isOther;
  private final long size;
  private final Object fileKey;

  GitFileAttributes(@Nonnull Map<String, Object> attributes) {
    lastModifiedTime = (FileTime) attributes.get(GitFileAttributeView.LAST_MODIFIED_TIME_NAME);
    lastAccessTime = (FileTime) attributes.get(GitFileAttributeView.LAST_ACCESS_TIME_NAME);
    creationTime = (FileTime) attributes.get(GitFileAttributeView.CREATION_TIME_NAME);
    isRegularFile = (boolean) attributes.get(GitFileAttributeView.IS_REGULAR_FILE_NAME);
    isDirectory = (boolean) attributes.get(GitFileAttributeView.IS_DIRECTORY_NAME);
    isSymbolicLink = (boolean) attributes.get(GitFileAttributeView.IS_SYMBOLIC_LINK_NAME);
    isOther = (boolean) attributes.get(GitFileAttributeView.IS_OTHER_NAME);
    size = (long) attributes.get(GitFileAttributeView.SIZE_NAME);
    fileKey = attributes.get(GitFileAttributeView.FILE_KEY_NAME);
  }

  /**
   * Returns {@link GitFileAttributeView#EPOCH EPOCH} as last modified time is not supported.
   *
   * @return  {@link GitFileAttributeView#EPOCH EPOCH}
   */
  @Nonnull
  @Override
  public FileTime lastModifiedTime() {
    return lastModifiedTime;
  }

  /**
   * Returns {@link GitFileAttributeView#EPOCH EPOCH} as last access time is not supported.
   *
   * @return  {@link GitFileAttributeView#EPOCH EPOCH}
   */
  @Nonnull
  @Override
  public FileTime lastAccessTime() {
    return lastAccessTime;
  }

  /**
   * Returns {@link GitFileAttributeView#EPOCH EPOCH} as creation time is not supported.
   *
   * @return  {@link GitFileAttributeView#EPOCH EPOCH}
   */
  @Nonnull
  @Override
  public FileTime creationTime() {
    return creationTime;
  }

  /**
   * Tells if the file is a regular file.
   *
   * @return  {@code true} if the file is a regular file
   */
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
