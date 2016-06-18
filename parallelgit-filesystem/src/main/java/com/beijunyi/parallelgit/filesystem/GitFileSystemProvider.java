package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.io.*;
import com.beijunyi.parallelgit.filesystem.utils.GfsConfiguration;
import com.beijunyi.parallelgit.filesystem.utils.GfsUriUtils;

import static java.nio.file.StandardOpenOption.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;

public class GitFileSystemProvider extends FileSystemProvider {

  public static final String GFS = "gfs";
  public static final String BRANCH = "branch";
  public static final String COMMIT = "commit";
  public static final Collection<OpenOption> SUPPORTED_OPEN_OPTIONS = supportedOpenOption();

  private static final GitFileSystemProvider INSTANCE = getInstalledProvider();
  private static final Map<String, GitFileSystem> FILE_SYSTEMS = new ConcurrentHashMap<>();

  @Nonnull
  public static GitFileSystemProvider getDefault() {
    return INSTANCE;
  }

  @Nonnull
  @Override
  public String getScheme() {
    return GFS;
  }

  @Nonnull
  @Override
  public GitFileSystem newFileSystem(Path path, Map<String, ?> properties) throws IOException {
    return newFileSystem(GfsConfiguration.fromPath(path, properties));
  }

  @Nonnull
  @Override
  public GitFileSystem newFileSystem(URI uri, Map<String, ?> properties) throws IOException {
    return newFileSystem(GfsConfiguration.fromUri(uri, properties));
  }

  @Nonnull
  public GitFileSystem newFileSystem(GfsConfiguration cfg) throws IOException {
    String sid = randomUUID().toString();
    GitFileSystem ret = new GitFileSystem(cfg, sid);
    FILE_SYSTEMS.put(sid, ret);
    return ret;
  }

  public void unregister(GitFileSystem gfs) {
    FILE_SYSTEMS.remove(gfs.getSessionId());
  }

  @Nonnull
  @Override
  public GitFileSystem getFileSystem(URI uri) {
    String session = GfsUriUtils.getSession(uri);
    if(session == null)
      throw new FileSystemNotFoundException();
    return getFileSystem(session);
  }

  @Nonnull
  public GitFileSystem getFileSystem(String sid) {
    GitFileSystem ret = FILE_SYSTEMS.get(sid);
    if(ret == null)
      throw new FileSystemNotFoundException(sid);
    return ret;
  }

  @Nonnull
  @Override
  public GitPath getPath(URI uri) throws FileSystemNotFoundException {
    GitFileSystem gfs = getFileSystem(uri);
    String file = GfsUriUtils.getFile(uri);
    return gfs.getPath(file).toRealPath();
  }

  @Nonnull
  @Override
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException, UnsupportedOperationException {
    Set<OpenOption> amended = new HashSet<>();
    for(OpenOption option : options) {
      if(!SUPPORTED_OPEN_OPTIONS.contains(option)) throw new UnsupportedOperationException(option.toString());
      if(option == APPEND) amended.add(WRITE);
      amended.add(option);
    }
    if(!amended.contains(WRITE)) amended.add(READ);
    return GfsIO.newByteChannel(((GitPath)path).toRealPath(), amended, asList(attrs));
  }

  @Nonnull
  @Override
  public GfsDirectoryStream newDirectoryStream(Path path, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    return GfsIO.newDirectoryStream(((GitPath)path).toRealPath(), filter);
  }

  @Override
  public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
    GfsIO.createDirectory((GitPath)dir);
  }

  @Override
  public void delete(Path path) throws IOException {
    GfsIO.delete(((GitPath)path).toRealPath());
  }

  @Override
  public void copy(Path source, Path target, CopyOption... options) throws IOException {
    GfsIO.copy((GitPath)source, (GitPath)target, new HashSet<>(asList(options)));
  }

  @Override
  public void move(Path source, Path target, CopyOption... options) throws IOException {
    GfsIO.move((GitPath)source, (GitPath)target, new HashSet<>(asList(options)));
  }

  @Override
  public boolean isSameFile(Path path, Path path2) {
    GitPath p1 = ((GitPath) path).toRealPath();
    GitPath p2 = ((GitPath) path).toRealPath();
    return Objects.equals(p1, p2);
  }

  @Override
  public boolean isHidden(Path path) {
    GitPath filename = ((GitPath) path).toRealPath().getFileName();
    return filename != null && filename.toString().charAt(0) == '.';
  }

  @Nonnull
  @Override
  public GfsFileStore getFileStore(Path path) {
    return ((GitPath) path).getFileStore();
  }

  @Override
  public void checkAccess(Path path, AccessMode... modes) throws IOException {
    GfsIO.checkAccess(((GitPath)path).toRealPath(), new HashSet<>(asList(modes)));
  }

  @Nullable
  @Override
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) throws UnsupportedOperationException {
    try {
      return GfsIO.getFileAttributeView(((GitPath)path).toRealPath(), type);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  @Override
  public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
    Class<? extends BasicFileAttributeView> viewType;
    if(type.isAssignableFrom(GfsFileAttributes.Basic.class)) {
      viewType = GfsFileAttributeView.Basic.class;
    } else if(type.isAssignableFrom(GfsFileAttributes.Posix.class)) {
      viewType = GfsFileAttributeView.Posix.class;
    } else if(type.isAssignableFrom(GfsFileAttributes.Git.class)) {
      viewType = GfsFileAttributeView.Git.class;
    } else {
      throw new UnsupportedOperationException(type.getName());
    }
    BasicFileAttributeView view = getFileAttributeView(path, viewType, options);
    if(view == null)
      throw new NoSuchFileException(path.toString());
    return type.cast(view.readAttributes());
  }

  @Nonnull
  @Override
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
    int viewNameEnd = attributes.indexOf(':');
    String viewName = viewNameEnd >= 0 ? attributes.substring(0, viewNameEnd) : GfsFileAttributeView.Basic.BASIC_VIEW;
    String keys = viewNameEnd >= 0 ? attributes.substring(viewNameEnd + 1) : attributes;
    Class<? extends GfsFileAttributeView> viewType;
    switch(viewName) {
      case GfsFileAttributeView.Basic.BASIC_VIEW:
        viewType = GfsFileAttributeView.Basic.class;
        break;
      case GfsFileAttributeView.Posix.POSIX_VIEW:
        viewType = GfsFileAttributeView.Posix.class;
        break;
      case GfsFileAttributeView.Git.GIT_VIEW:
        viewType = GfsFileAttributeView.Git.class;
        break;
      default:
        throw new UnsupportedOperationException("View \"" + viewName + "\" is not available");
    }
    GfsFileAttributeView view = getFileAttributeView(path, viewType, options);
    if(view == null)
      throw new NoSuchFileException(path.toString());
    return view.readAttributes(asList(keys.split(",")));
  }

  @Override
  public void setAttribute(@Nullable Path path, @Nullable String attribute, @Nullable Object value, @Nullable LinkOption... options) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  private static GitFileSystemProvider getInstalledProvider() {
    GitFileSystemProvider ret = null;
    for(FileSystemProvider provider : FileSystemProvider.installedProviders()) {
      if(provider instanceof GitFileSystemProvider) {
        ret = (GitFileSystemProvider) provider;
        break;
      }
    }
    if(ret == null)
      ret = new GitFileSystemProvider();
    return ret;
  }

  @Nonnull
  private static Collection<OpenOption> supportedOpenOption() {
    List<OpenOption> options = Arrays.<OpenOption>asList(READ, SPARSE, CREATE, CREATE_NEW, WRITE, APPEND, TRUNCATE_EXISTING);
    return unmodifiableList(options);
  }

}
