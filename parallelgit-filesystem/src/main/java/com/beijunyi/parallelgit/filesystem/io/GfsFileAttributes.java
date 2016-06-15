package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.*;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import static com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView.Basic.BASIC_KEYS;
import static com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView.*;
import static com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView.Git.GIT_KEYS;
import static com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView.Posix.POSIX_KEYS;

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

    private Basic(Map<String, Object> attributes) {
      lastModifiedTime = (FileTime) attributes.get(LAST_MODIFIED_TIME_NAME);
      lastAccessTime = (FileTime) attributes.get(LAST_ACCESS_TIME_NAME);
      creationTime = (FileTime) attributes.get(CREATION_TIME_NAME);
      isRegularFile = (boolean) attributes.get(IS_REGULAR_FILE_NAME);
      isDirectory = (boolean) attributes.get(IS_DIRECTORY_NAME);
      isSymbolicLink = (boolean) attributes.get(IS_SYMBOLIC_LINK_NAME);
      isOther = (boolean) attributes.get(IS_OTHER_NAME);
      size = (long) attributes.get(SIZE_NAME);
      fileKey = attributes.get(FILE_KEY_NAME);
    }

    Basic(GfsFileAttributeView view) throws IOException {
      this(view.readAttributes(BASIC_KEYS));
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

    @Override
    public boolean isDirectory() {
      return isDirectory;
    }

    @Override
    public boolean isSymbolicLink() {
      return isSymbolicLink;
    }

    @Override
    public boolean isOther() {
      return isOther;
    }

    @Override
    public long size() {
      return size;
    }

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
    private Posix(Map<String, Object> attributes) {
      super(attributes);
      owner = (UserPrincipal) attributes.get(GfsFileAttributeView.Posix.OWNER_NAME);
      group = (GroupPrincipal) attributes.get(GfsFileAttributeView.Posix.GROUP_NAME);
      permissions = (Set<PosixFilePermission>) attributes.get(GfsFileAttributeView.Posix.PERMISSIONS_NAME);
    }

    Posix(GfsFileAttributeView view) throws IOException {
      this(view.readAttributes(POSIX_KEYS));
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

  public static class Git extends Posix implements GitFileAttributes {

    private final boolean isNew;
    private final boolean isModified;
    private final ObjectId objectId;
    private final FileMode fileMode;

    public Git(Map<String, Object> attributes) throws IOException {
      super(attributes);
      isNew = (boolean) attributes.get(GfsFileAttributeView.IS_NEW);
      isModified = (boolean) attributes.get(GfsFileAttributeView.IS_MODIFIED);
      objectId = (ObjectId) attributes.get(GfsFileAttributeView.OBJECT_ID);
      fileMode = (FileMode) attributes.get(GfsFileAttributeView.FILE_MODE);
    }

    public Git(GfsFileAttributeView.Git view) throws IOException {
      this(view.readAttributes(GIT_KEYS));
    }

    @Override
    public boolean isNew() throws IOException {
      return isNew;
    }

    @Override
    public boolean isModified() throws IOException {
      return isModified;
    }

    @Nullable
    @Override
    public ObjectId getObjectId() throws IOException {
      return objectId;
    }

    @Nonnull
    @Override
    public FileMode getFileMode() {
      return fileMode;
    }
  }

}
