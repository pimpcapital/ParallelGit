package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.*;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;

public abstract class GfsFileAttributeView implements FileAttributeView {

  protected final Node node;
  protected final GitFileSystem gfs;

  protected GfsFileAttributeView(@Nonnull Node node, @Nonnull GitFileSystem gfs) {
    this.node = node;
    this.gfs = gfs;
  }

  @Nonnull
  static <V extends FileAttributeView> V forNode(@Nonnull Node node, @Nonnull GitFileSystem gfs, @Nonnull Class<V> type) throws UnsupportedOperationException {
    if(type.isAssignableFrom(GfsFileAttributeView.Basic.class))
      return type.cast(new GfsFileAttributeView.Basic(node, gfs));
    if(type.isAssignableFrom(GfsFileAttributeView.Posix.class))
      return type.cast(new GfsFileAttributeView.Posix(node, gfs));
    throw new UnsupportedOperationException(type.getName());
  }

  @Nonnull
  public abstract Map<String, Object> readAttributes(@Nonnull Collection<String> attributes) throws IOException;

  public static class Basic extends GfsFileAttributeView implements BasicFileAttributeView {

    private static final FileTime EPOCH = FileTime.fromMillis(0);

    public static final String SIZE_NAME = "size";
    public static final String CREATION_TIME_NAME = "creationTime";
    public static final String LAST_ACCESS_TIME_NAME = "lastAccessTime";
    public static final String LAST_MODIFIED_TIME_NAME = "lastModifiedTime";
    public static final String FILE_KEY_NAME = "fileKey";
    public static final String IS_DIRECTORY_NAME = "isDirectory";
    public static final String IS_REGULAR_FILE_NAME = "isRegularFile";
    public static final String IS_SYMBOLIC_LINK_NAME = "isSymbolicLink";
    public static final String IS_OTHER_NAME = "isOther";

    public static final String BASIC_VIEW = "basic";
    public static final Collection<String> BASIC_KEYS =
      Collections.unmodifiableCollection(Arrays.asList(
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

    protected Basic(@Nonnull Node node, @Nonnull GitFileSystem gfs) {
      super(node, gfs);
    }

    @Nonnull
    @Override
    public String name() {
      return BASIC_VIEW;
    }

    @Nonnull
    @Override
    public BasicFileAttributes readAttributes() throws IOException {
      return new GfsFileAttributes.Basic(readAttributes(BASIC_KEYS));
    }

    @Override
    public void setTimes(@Nonnull FileTime lastModifiedTime, @Nonnull FileTime lastAccessTime, @Nonnull FileTime createTime) {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Map<String, Object> readAttributes(@Nonnull Collection<String> keys) throws IOException {
      Map<String, Object> result = new HashMap<>();
      for(String key : keys) {
        switch(key) {
          case SIZE_NAME:
            result.put(key, GfsIO.getSize(node, gfs));
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
      return Collections.unmodifiableMap(result);
    }
  }

  public static class Posix extends Basic implements PosixFileAttributeView {

    private static final Collection<PosixFilePermission> DEFAULT_PERMISSIONS =
      Collections.unmodifiableCollection(Arrays.asList(
                                                        PosixFilePermission.OWNER_READ,
                                                        PosixFilePermission.OWNER_WRITE
      ));

    public static final String PERMISSIONS_NAME = "permissions";
    public static final String OWNER_NAME = "owner";
    public static final String GROUP_NAME = "group";

    public static final String POSIX_VIEW = "posix";
    public static final Collection<String> POSIX_KEYS =
      Collections.unmodifiableCollection(Arrays.asList(
                                                        SIZE_NAME,
                                                        CREATION_TIME_NAME,
                                                        LAST_ACCESS_TIME_NAME,
                                                        LAST_MODIFIED_TIME_NAME,
                                                        FILE_KEY_NAME,
                                                        IS_DIRECTORY_NAME,
                                                        IS_REGULAR_FILE_NAME,
                                                        IS_SYMBOLIC_LINK_NAME,
                                                        IS_OTHER_NAME,
                                                        PERMISSIONS_NAME,
                                                        OWNER_NAME,
                                                        GROUP_NAME
      ));

    protected Posix(@Nonnull Node node, @Nonnull GitFileSystem gfs) {
      super(node, gfs);
    }

    @Nonnull
    @Override
    public String name() {
      return POSIX_VIEW;
    }

    @Nonnull
    @Override
    public PosixFileAttributes readAttributes() throws IOException {
      return new GfsFileAttributes.Posix(readAttributes(POSIX_KEYS));
    }

    @Nonnull
    public Set<PosixFilePermission> getPermissions() throws IOException {
      Set<PosixFilePermission> perms = new HashSet<>(DEFAULT_PERMISSIONS);
      if(node.isExecutableFile())
        perms.add(PosixFilePermission.OWNER_EXECUTE);
      return Collections.unmodifiableSet(perms);
    }


    @Override
    public void setPermissions(@Nonnull Set<PosixFilePermission> perms) throws IOException {
      NodeType type = perms.contains(PosixFilePermission.OWNER_EXECUTE) ? NodeType.EXECUTABLE_FILE : NodeType.NON_EXECUTABLE_FILE;
      if(!type.equals(node.getType())) {
        if(node.isRegularFile())
          node.setType(type);
        else
          throw new IllegalArgumentException();
      }

    }

    @Override
    public void setGroup(@Nonnull GroupPrincipal group) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public UserPrincipal getOwner() throws IOException {
      return null;
    }

    @Override
    public void setOwner(@Nonnull UserPrincipal owner) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Map<String, Object> readAttributes(@Nonnull Collection<String> keys) throws IOException {
      Set<String> basicKeys = new HashSet<>(keys);
      basicKeys.retainAll(BASIC_KEYS);
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
      return Collections.unmodifiableMap(result);
    }
  }

}
