package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.*;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.FileMode;

import static java.nio.file.attribute.FileTime.fromMillis;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.eclipse.jgit.lib.FileMode.*;

public abstract class GfsFileAttributeView implements FileAttributeView {

  public static final String SIZE_NAME = "size";
  public static final String CREATION_TIME_NAME = "creationTime";
  public static final String LAST_ACCESS_TIME_NAME = "lastAccessTime";
  public static final String LAST_MODIFIED_TIME_NAME = "lastModifiedTime";
  public static final String FILE_KEY_NAME = "fileKey";
  public static final String IS_DIRECTORY_NAME = "isSubtree";
  public static final String IS_REGULAR_FILE_NAME = "isRegularFile";
  public static final String IS_SYMBOLIC_LINK_NAME = "isSymbolicLink";
  public static final String IS_OTHER_NAME = "isOther";

  public static final String PERMISSIONS_NAME = "permissions";
  public static final String OWNER_NAME = "owner";
  public static final String GROUP_NAME = "group";

  public static final String IS_NEW = "isNew";
  public static final String IS_MODIFIED = "isModified";
  public static final String OBJECT_ID = "objectId";
  public static final String FILE_MODE = "fileMode";

  protected final Node node;

  protected GfsFileAttributeView(Node node) {
    this.node = node;
  }

  @Nonnull
  static <V extends FileAttributeView> V forNode(Node node, Class<V> type) throws UnsupportedOperationException {
    if(type.isAssignableFrom(GfsFileAttributeView.Basic.class))
      return type.cast(new GfsFileAttributeView.Basic(node));
    if(type.isAssignableFrom(GfsFileAttributeView.Posix.class))
      return type.cast(new GfsFileAttributeView.Posix(node));
    if(type.isAssignableFrom(GfsFileAttributeView.Git.class))
      return type.cast(new GfsFileAttributeView.Git(node));
    throw new UnsupportedOperationException(type.getName());
  }

  @Nonnull
  public abstract Map<String, Object> readAttributes(Collection<String> attributes) throws IOException;

  public static class Basic extends GfsFileAttributeView implements BasicFileAttributeView {

    public static final String BASIC_VIEW = "basic";
    public static final Set<String> BASIC_KEYS = keys();
    private static final FileTime EPOCH = fromMillis(0);

    protected Basic(Node node) {
      super(node);
    }

    @Nonnull
    @Override
    public String name() {
      return BASIC_VIEW;
    }

    @Nonnull
    @Override
    public BasicFileAttributes readAttributes() throws IOException {
      return new GfsFileAttributes.Basic(this);
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Map<String, Object> readAttributes(Collection<String> keys) throws IOException {
      Map<String, Object> result = new HashMap<>();
      for(String key : keys) {
        switch(key) {
          case SIZE_NAME:
            result.put(key, node.getSize());
            break;
          case CREATION_TIME_NAME:
            result.put(key, EPOCH);
            break;
          case LAST_ACCESS_TIME_NAME:
            result.put(key, EPOCH);
            break;
          case LAST_MODIFIED_TIME_NAME:
            result.put(key, EPOCH);
            break;
          case FILE_KEY_NAME:
            result.put(key, null);
            break;
          case IS_DIRECTORY_NAME:
            result.put(key, node.isDirectory());
            break;
          case IS_REGULAR_FILE_NAME:
            result.put(key, node.isRegularFile());
            break;
          case IS_SYMBOLIC_LINK_NAME:
            result.put(key, node.isSymbolicLink());
            break;
          case IS_OTHER_NAME:
            result.put(key, false);
            break;
          default:
            throw new UnsupportedOperationException("Attribute \"" + key + "\" is not supported");
        }
      }
      return unmodifiableMap(result);
    }

    @Nonnull
    private static Set<String> keys() {
      Set<String> ret = new HashSet<>(asList(
        SIZE_NAME,
        CREATION_TIME_NAME,
        LAST_ACCESS_TIME_NAME,
        LAST_MODIFIED_TIME_NAME,
        FILE_KEY_NAME,
        IS_DIRECTORY_NAME,
        IS_REGULAR_FILE_NAME,
        IS_SYMBOLIC_LINK_NAME,
        IS_OTHER_NAME
      ));
      return unmodifiableSet(ret);
    }

  }

  public static class Posix extends Basic implements PosixFileAttributeView {

    public static final String POSIX_VIEW = "posix";
    public static final Set<String> POSIX_KEYS = keys();
    private static final Collection<PosixFilePermission> DEFAULT_PERMISSIONS = defaultPermissions();

    protected Posix(Node node) {
      super(node);
    }

    @Nonnull
    @Override
    public String name() {
      return POSIX_VIEW;
    }

    @Nonnull
    @Override
    public PosixFileAttributes readAttributes() throws IOException {
      return new GfsFileAttributes.Posix(this);
    }

    @Nonnull
    public Set<PosixFilePermission> getPermissions() throws IOException {
      Set<PosixFilePermission> perms = new HashSet<>(DEFAULT_PERMISSIONS);
      if(node.isExecutableFile())
        perms.add(OWNER_EXECUTE);
      return unmodifiableSet(perms);
    }


    @Override
    public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
      FileMode mode = perms.contains(OWNER_EXECUTE) ? EXECUTABLE_FILE : REGULAR_FILE;
      node.setMode(mode);
    }

    @Override
    public void setGroup(GroupPrincipal group) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public UserPrincipal getOwner() throws IOException {
      return null;
    }

    @Override
    public void setOwner(UserPrincipal owner) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Map<String, Object> readAttributes(Collection<String> keys) throws IOException {
      Set<String> basicKeys = new HashSet<>(keys);
      basicKeys.retainAll(Basic.BASIC_KEYS);
      Map<String, Object> result = new HashMap<>(super.readAttributes(basicKeys));
      Set<String> remainKeys = new HashSet<>(keys);
      remainKeys.removeAll(result.keySet());
      for(String key : remainKeys) {
        switch(key) {
          case PERMISSIONS_NAME:
            result.put(key, getPermissions());
            break;
          case OWNER_NAME:
            result.put(key, getOwner());
            break;
          case GROUP_NAME:
            result.put(key, null);
            break;
          default:
            throw new UnsupportedOperationException("Attribute \"" + key + "\" is not supported");
        }
      }
      return unmodifiableMap(result);
    }

    @Nonnull
    private static Collection<PosixFilePermission> defaultPermissions() {
      return unmodifiableCollection(asList(OWNER_READ, OWNER_WRITE));
    }

    @Nonnull
    private static Set<String> keys() {
      Set<String> ret = new HashSet<>();
      ret.addAll(Basic.BASIC_KEYS);
      ret.addAll(asList(PERMISSIONS_NAME, OWNER_NAME, GROUP_NAME));
      return unmodifiableSet(ret);
    }

  }

  public static class Git extends Posix implements GitFileAttributeView {

    public static final String GIT_VIEW = "git";
    public static final Set<String> GIT_KEYS = Git.keys();

    protected Git(Node node) {
      super(node);
    }

    @Nonnull
    @Override
    public String name() {
      return GIT_VIEW;
    }

    @Override
    public void setFileMode(FileMode mode) {
      node.setMode(mode);
    }

    @Nonnull
    @Override
    public GitFileAttributes readAttributes() throws IOException {
      return new GfsFileAttributes.Git(this);
    }

    @Nonnull
    @Override
    public Map<String, Object> readAttributes(Collection<String> keys) throws IOException {
      Set<String> posixKeys = new HashSet<>(keys);
      posixKeys.retainAll(Posix.POSIX_KEYS);
      Map<String, Object> result = new HashMap<>(super.readAttributes(posixKeys));
      Set<String> remainKeys = new HashSet<>(keys);
      remainKeys.removeAll(result.keySet());
      for(String key : remainKeys) {
        switch(key) {
          case IS_NEW:
            result.put(key, node.isNew());
            break;
          case IS_MODIFIED:
            result.put(key, node.isModified());
            break;
          case OBJECT_ID:
            result.put(key, node.getObjectId(false));
            break;
          case FILE_MODE:
            result.put(key, node.getMode());
            break;
          default:
            throw new UnsupportedOperationException(key);
        }
      }
      return unmodifiableMap(result);
    }

    @Nonnull
    private static Set<String> keys() {
      Set<String> ret = new HashSet<>();
      ret.addAll(Posix.POSIX_KEYS);
      ret.addAll(asList(IS_NEW, IS_MODIFIED, OBJECT_ID, FILE_MODE));
      return unmodifiableSet(ret);
    }

  }

}
