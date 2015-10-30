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
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import com.beijunyi.parallelgit.filesystem.utils.GitUriUtils;

import static java.nio.file.StandardOpenOption.*;

public class GitFileSystemProvider extends FileSystemProvider {

  public final static String GIT_FS_SCHEME = "gfs";

  public final static Set<OpenOption> SUPPORTED_OPEN_OPTIONS = new HashSet<>(Arrays.<OpenOption>asList(READ, SPARSE, CREATE, CREATE_NEW, WRITE, APPEND, TRUNCATE_EXISTING));

  private final Map<String, GitFileSystem> fsMap = new ConcurrentHashMap<>();

  private static GitFileSystemProvider INSTANCE;

  @Nonnull
  public static GitFileSystemProvider getInstance() {
    if(INSTANCE == null) {
      for(FileSystemProvider provider : FileSystemProvider.installedProviders()) {
        if(provider instanceof GitFileSystemProvider) {
          INSTANCE = (GitFileSystemProvider) provider;
          break;
        }
      }
      if(INSTANCE == null)
        throw new ProviderNotFoundException(GIT_FS_SCHEME);
    }
    return INSTANCE;
  }

  @Nonnull
  @Override
  public String getScheme() {
    return GIT_FS_SCHEME;
  }

  @Nonnull
  @Override
  public GitFileSystem newFileSystem(@Nonnull Path path, @Nonnull Map<String, ?> properties) throws IOException {
    return GitFileSystemBuilder
             .fromPath(path, properties)
             .provider(this)
             .build();
  }

  @Nonnull
  @Override
  public GitFileSystem newFileSystem(@Nonnull URI uri, @Nonnull Map<String, ?> properties) throws IOException {
    return GitFileSystemBuilder
             .fromUri(uri, properties)
             .provider(this)
             .build();
  }

  public void register(@Nonnull GitFileSystem gfs) {
    fsMap.put(gfs.getSessionId(), gfs);
  }

  public void unregister(@Nonnull GitFileSystem gfs) {
    fsMap.remove(gfs.getSessionId());
  }

  @Nullable
  public GitFileSystem getFileSystem(@Nonnull String sessionId) {
    return fsMap.get(sessionId);
  }

  @Nullable
  @Override
  public GitFileSystem getFileSystem(@Nonnull URI uri) {
    String session = GitUriUtils.getSession(uri);
    if(session == null)
      return null;
    return getFileSystem(session);
  }

  @Nonnull
  @Override
  public GitPath getPath(@Nonnull URI uri) throws FileSystemNotFoundException {
    GitFileSystem gfs = getFileSystem(uri);
    if(gfs == null)
      throw new FileSystemNotFoundException(uri.toString());
    String file = GitUriUtils.getFile(uri);
    return gfs.getPath(file).toRealPath();
  }

  @Nonnull
  @Override
  public SeekableByteChannel newByteChannel(@Nonnull Path path, @Nonnull Set<? extends OpenOption> options, @Nonnull FileAttribute<?>... attrs) throws IOException, UnsupportedOperationException {
    Set<OpenOption> amended = new HashSet<>();
    for(OpenOption option : options) {
      if(!SUPPORTED_OPEN_OPTIONS.contains(option))
        throw new UnsupportedOperationException(option.toString());
      if(option == APPEND)
        amended.add(WRITE);
      amended.add(option);
    }
    if(!amended.contains(WRITE))
      amended.add(READ);
    return GfsIO.newByteChannel(((GitPath)path).toRealPath(), amended, Arrays.<FileAttribute>asList(attrs));
  }

  @Nonnull
  @Override
  public GfsDirectoryStream newDirectoryStream(@Nonnull Path path, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    return GfsIO.newDirectoryStream(((GitPath)path).toRealPath(), filter);
  }

  @Override
  public void createDirectory(@Nonnull Path dir, @Nonnull FileAttribute<?>... attrs) throws IOException {
    GfsIO.createDirectory((GitPath)dir);
  }

  @Override
  public void delete(@Nonnull Path path) throws IOException {
    GfsIO.delete(((GitPath)path).toRealPath());
  }

  @Override
  public void copy(@Nonnull Path source, @Nonnull Path target, @Nonnull CopyOption... options) throws IOException {
    GfsIO.copy((GitPath)source, (GitPath)target, new HashSet<>(Arrays.asList(options)));
  }

  @Override
  public void move(@Nonnull Path source, @Nonnull Path target, @Nonnull CopyOption... options) throws IOException {
    GfsIO.move((GitPath)source, (GitPath)target, new HashSet<>(Arrays.asList(options)));
  }

  @Override
  public boolean isSameFile(@Nonnull Path path, @Nonnull Path path2) {
    GitPath p1 = ((GitPath) path).toRealPath();
    GitPath p2 = ((GitPath) path).toRealPath();
    return Objects.equals(p1, p2);
  }

  @Override
  public boolean isHidden(@Nonnull Path path) {
    GitPath filename = ((GitPath) path).toRealPath().getFileName();
    return filename != null && filename.toString().charAt(0) == '.';
  }

  @Nonnull
  @Override
  public GitFileStore getFileStore(@Nonnull Path path) {
    return ((GitPath) path).getFileStore();
  }

  @Override
  public void checkAccess(@Nonnull Path path, @Nonnull AccessMode... modes) throws IOException {
    GfsIO.checkAccess(((GitPath)path).toRealPath(), new HashSet<>(Arrays.asList(modes)));
  }

  @Nullable
  @Override
  public <V extends FileAttributeView> V getFileAttributeView(@Nonnull Path path, @Nonnull Class<V> type, @Nonnull LinkOption... options) throws UnsupportedOperationException {
    try {
      return GfsIO.getFileAttributeView(((GitPath)path).toRealPath(), type);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  @Override
  public <A extends BasicFileAttributes> A readAttributes(@Nonnull Path path, @Nonnull Class<A> type, @Nonnull LinkOption... options) throws IOException {
    Class<? extends BasicFileAttributeView> viewType;
    if(type.isAssignableFrom(GfsFileAttributes.Basic.class))
      viewType = GfsFileAttributeView.Basic.class;
    else if(type.isAssignableFrom(GfsFileAttributes.Posix.class))
      viewType = GfsFileAttributeView.Posix.class;
    else
      throw new UnsupportedOperationException(type.getName());
    BasicFileAttributeView view = getFileAttributeView(path, viewType, options);
    if(view == null)
      throw new NoSuchFileException(path.toString());
    return type.cast(view.readAttributes());
  }

  @Nonnull
  @Override
  public Map<String, Object> readAttributes(@Nonnull Path path, @Nonnull String attributes, @Nonnull LinkOption... options) throws IOException {
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
      default:
        throw new UnsupportedOperationException("View \"" + viewName + "\" is not available");
    }
    GfsFileAttributeView view = getFileAttributeView(path, viewType, options);
    if(view == null)
      throw new NoSuchFileException(path.toString());
    return view.readAttributes(Arrays.asList(keys.split(",")));
  }

  @Override
  public void setAttribute(@Nullable Path path, @Nullable String attribute, @Nullable Object value, @Nullable LinkOption... options) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

}
