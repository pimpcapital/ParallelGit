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

import com.beijunyi.parallelgit.filesystem.io.GitDirectoryStream;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import com.beijunyi.parallelgit.filesystem.utils.GitUriUtils;
import com.beijunyi.parallelgit.filesystem.utils.IOUtils;

import static java.nio.file.StandardOpenOption.*;

public class GitFileSystemProvider extends FileSystemProvider {

  public final static String GIT_FS_SCHEME = "gfs";

  public final static EnumSet<StandardOpenOption> SUPPORTED_OPEN_OPTIONS = EnumSet.of(READ, SPARSE, CREATE, CREATE_NEW, WRITE, APPEND, TRUNCATE_EXISTING);

  private final Map<String, GitFileSystem> fsMap = new ConcurrentHashMap<>();

  private static GitFileSystemProvider INSTANCE;

  /**
   * Returns the {@link FileSystemProvider#installedProviders() installed} {@code GitFileSystemProvider} instance or
   * throws a {@code ProviderNotFoundException} if none is installed.
   *
   * @return  the installed {@code GitFileSystemProvider} instance
   */
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

  /**
   * Returns {@code "gfs"} as the scheme of this provider.
   *
   * @return  {@code "gfs"}
   */
  @Nonnull
  @Override
  public String getScheme() {
    return GIT_FS_SCHEME;
  }

  @Nonnull
  @Override
  public GitFileSystem newFileSystem(@Nonnull Path path, @Nonnull Map<String, ?> properties) throws IOException {
    return GitFileSystemBuilder
             .forPath(path, properties)
             .provider(this)
             .build();
  }

  @Nonnull
  @Override
  public GitFileSystem newFileSystem(@Nonnull URI uri, @Nonnull Map<String, ?> properties) throws IOException {
    return GitFileSystemBuilder
             .forUri(uri, properties)
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

  /**
   * Return a {@code GitPath} object by converting the given {@link URI}. The resulting {@code GitPath} is associated
   * with a {@link GitFileSystem} that already exists or is constructed automatically.
   *
   * @param   uri
   *          The URI to convert
   *
   * @throws  ProviderMismatchException
   *          if the {@code URI} scheme is not specified or is not equal to {@link #GIT_FS_SCHEME "git"}.
   */
  @Nonnull
  @Override
  public GitPath getPath(@Nonnull URI uri) throws FileSystemNotFoundException {
    GitFileSystem gfs = getFileSystem(uri);
    if(gfs == null)
      throw new FileSystemNotFoundException(uri.toString());
    String file = GitUriUtils.getFile(uri);
    return gfs.getPath(file).toRealPath();
  }

  /**
   * Opens or creates a file, returning a {@code GitSeekableByteChannel} to access the file. This method works in
   * exactly the manner specified by the {@link Files#newByteChannel(Path,Set,FileAttribute[])} method.
   *
   * @param   path
   *          the path to the file to open or create
   * @param   options
   *          options specifying how the file is opened
   * @param   attrs
   *          an optional list of file attributes to set atomically when creating the file
   *
   * @return  a {@code GitSeekableByteChannel} to access the target file
   *
   * @throws  NoSuchFileException
   *          if the target file does not exists and neither {@link StandardOpenOption#CREATE} nor {@link
   *          StandardOpenOption#CREATE_NEW} option is specified
   * @throws  AccessDeniedException
   *          if the target file is a directory
   * @throws  FileAlreadyExistsException
   *          if the target file already exists and {@link StandardOpenOption#CREATE_NEW} option is specified
   */
  @Nonnull
  @Override
  public SeekableByteChannel newByteChannel(@Nonnull Path path, @Nonnull Set<? extends OpenOption> options, @Nonnull FileAttribute<?>... attrs) throws IOException {
    return IOUtils.newByteChannel(((GitPath) path).toRealPath(), new HashSet<>(options), Arrays.<FileAttribute>asList(attrs));
  }

  /**
   * Opens a directory, returning a {@code GitDirectoryStream} to iterate over the entries in the directory. This method
   * works in exactly the manner specified by the {@link Files#newDirectoryStream(Path, DirectoryStream.Filter)} method.
   *
   * @param   path
   *          the path to the directory
   * @param   filter
   *          the directory stream filter
   *
   * @return  a new and open {@code GitDirectoryStream} object
   *
   * @throws  NotDirectoryException
   *          if the file could not otherwise be opened because it is not a directory
   */
  @Nonnull
  @Override
  public GitDirectoryStream newDirectoryStream(@Nonnull Path path, @Nullable DirectoryStream.Filter<? super Path> filter) throws IOException {
    return IOUtils.newDirectoryStream(((GitPath) path).toRealPath(), filter);
  }

  /**
   * Calling this method has no effect as an empty directory is trivial to a git repository. However, if the target
   * already exists (regardless of whether it is a file or a non-empty directory), this method throws a {@code
   * FileAlreadyExistsException} to indicate the error.
   *
   * Creating a file inside a directory does not require to explicitly create its parent directory. The necessary
   * hierarchy is automatically created when a file is created.
   *
   * @param   dir
   *          the target path
   * @param   attrs
   *          unused parameter
   * @throws  FileAlreadyExistsException
   *          if a file of the target path already exists
   */
  @Override
  public void createDirectory(@Nonnull Path dir, @Nonnull FileAttribute<?>... attrs) throws IOException {
    IOUtils.createDirectory((GitPath) dir);
  }

  /**
   * Deletes a file. This method works in exactly the  manner specified by the {@link Files#delete} method.
   *
   * @param   path
   *          the path to the file to delete
   * @throws  AccessDeniedException
   *          if the target file is associated with an open {@code GitSeekableByteChannel} or its parent directories are
   *          associated with an open {@code GitDirectoryStream}
   * @throws  NoSuchFileException
   *          if the target file does not exist
   * @throws  DirectoryNotEmptyException
   *          if the target file is a directory and could not otherwise be deleted because the directory is not empty
   */
  @Override
  public void delete(@Nonnull Path path) throws IOException {
    IOUtils.delete(((GitPath) path).toRealPath());
  }

  /**
   * Copy a file to a target file. This method works in exactly the manner specified by the {@link
   * Files#copy(Path,Path,CopyOption[])} method except that both the source and target paths must be associated with
   * this provider.
   *
   * @param   source
   *          the path to the file to copy
   * @param   target
   *          the path to the target file
   * @param   options
   *          options specifying how the copy should be done
   * @throws  AccessDeniedException
   *          if the file to copy or the target file is associated with an open {@code GitSeekableByteChannel} or their
   *          parent directories are associated with an open {@code GitDirectoryStream}
   * @throws  NoSuchFileException
   *          if the file to copy does not exist
   * @throws  FileAlreadyExistsException
   *          if the target file exists but cannot be replaced because the {@code REPLACE_EXISTING} option is not
   *          specified
   * @throws  DirectoryNotEmptyException
   *          the {@code REPLACE_EXISTING} option is specified but the file cannot be replaced because it is a non-empty
   *          directory
   * @throws  IOException
   *          if an I/O error occurs
   */
  @Override
  public void copy(@Nonnull Path source, @Nonnull Path target, @Nonnull CopyOption... options) throws IOException {
    IOUtils.copy((GitPath) source, (GitPath) target, new HashSet<>(Arrays.asList(options)));
  }

  /**
   * Move or rename a file to a target file. This method works in exactly the manner specified by the {@link Files#move}
   * method except that both the source and target paths must be associated with this provider.
   *
   * @param   source
   *          the path to the file to move
   * @param   target
   *          the path to the target file
   * @param   options
   *          options specifying how the move should be done
   * @throws  NoSuchFileException
   *          if the file to move does not exist
   * @throws  AccessDeniedException
   *          if the file to move or the target file is associated with an open {@code GitSeekableByteChannel} or their
   *          parent directories are associated with an open {@code GitDirectoryStream}
   * @throws  FileAlreadyExistsException
   *          if the target file exists but cannot be replaced because the {@code REPLACE_EXISTING} option is not
   *          specified
   * @throws  DirectoryNotEmptyException
   *          the {@code REPLACE_EXISTING} option is specified but the file cannot be replaced because it is a non-empty
   *          directory
   * @throws  IOException
   *          if an I/O error occurs
   */
  @Override
  public void move(@Nonnull Path source, @Nonnull Path target, @Nonnull CopyOption... options) throws IOException {
    IOUtils.move((GitPath) source, (GitPath) target, new HashSet<>(Arrays.asList(options)));
  }

  /**
   * Tests if two paths locate the same file. This method works in exactly the manner specified by the {@link
   * Files#isSameFile} method.
   *
   * @param   path
   *          one path to the file
   * @param   path2
   *          the other path
   * @return  {@code true} if the two paths locate the same file
   */
  @Override
  public boolean isSameFile(@Nonnull Path path, @Nonnull Path path2) {
    GitPath p1 = ((GitPath) path).toRealPath();
    GitPath p2 = ((GitPath) path).toRealPath();
    return Objects.equals(p1, p2);
  }

  /**
   * Tests whether or not a file is considered {@code hidden}. This method works in exactly the manner specified by the
   * {@link Files#isHidden} method.
   *
   * @param   path
   *          the path to the file to test
   * @return  {@code true} if the specified file is considered hidden
   */
  @Override
  public boolean isHidden(@Nonnull Path path) {
    GitPath filename = ((GitPath) path).toRealPath().getFileName();
    return filename != null && filename.toString().charAt(0) == '.';
  }

  /**
   * Returns the {@code GitFileStore} representing the file store where a file is located. This method works in exactly
   * the manner specified by the {@link Files#getFileStore} method.
   *
   * @param   path
   *          the path to the file
   * @return  the file store where the file is stored
   */
  @Nonnull
  @Override
  public GitFileStore getFileStore(@Nonnull Path path) {
    return ((GitPath) path).getFileStore();
  }

  /**
   * Checks the existence and the accessibility of a file.
   *
   * This method is used by the {@link Files#isReadable(Path)}, {@link Files#isWritable(Path)} and {@link
   * Files#isExecutable(Path)} methods to check the accessibility of a file.
   *
   * @param   path
   *          the path to the file to check
   * @param   modes
   *          The access modes to check
   *
   * @throws  NoSuchFileException
   *          if the target file does not exist
   * @throws  AccessDeniedException
   *          if any of the requested access modes to the target file is denied
   */
  @Override
  public void checkAccess(@Nonnull Path path, @Nonnull AccessMode... modes) throws IOException {
    IOUtils.checkAccess(((GitPath) path).toRealPath(), new HashSet<>(Arrays.asList(modes)));
  }

  /**
   * Returns a file attribute view of a given type. This method works in exactly the manner specified by the {@link
   * Files#getFileAttributeView} method.
   *
   * @param   path
   *          the path to the file
   * @param   type
   *          the {@code Class} object corresponding to the file attribute view
   * @param   options
   *          unused argument
   *
   * @return  a file attribute view of the specified type
   *
   * @throws  UnsupportedOperationException
   *          if file attribute view of the specified type is not supported
   */
  @Nullable
  @Override
  public <V extends FileAttributeView> V getFileAttributeView(@Nonnull Path path, @Nonnull Class<V> type, @Nonnull LinkOption... options) throws UnsupportedOperationException {
    try {
      return IOUtils.getFileAttributeView(((GitPath) path).toRealPath(), type);
    } catch(IOException e) {
      return null;
    }
  }

  /**
   * Reads a file's attributes as a bulk operation. This method works in exactly the manner specified by the {@link
   * Files#readAttributes(Path,Class,LinkOption[])} method.
   *
   * @param   path
   *          the path to the file
   * @param   type
   *          the {@code Class} of the file attributes required to read
   * @param   options
   *          unused argument
   *
   * @return  the file attributes
   *
   * @throws  NoSuchFileException
   *          if the target file does not exist
   * @throws  UnsupportedOperationException
   *          if an attributes of the given type are not supported
   */
  @Nonnull
  @Override
  public <A extends BasicFileAttributes> A readAttributes(@Nonnull Path path, @Nonnull Class<A> type, @Nonnull LinkOption... options) throws IOException {
    Class<? extends BasicFileAttributeView> viewType;
    if(type.isAssignableFrom(GitFileAttributes.Basic.class))
      viewType = GitFileAttributeView.Basic.class;
    else if(type.isAssignableFrom(GitFileAttributes.Posix.class))
      viewType = GitFileAttributeView.Posix.class;
    else
      throw new UnsupportedOperationException(type.getName());
    BasicFileAttributeView view = getFileAttributeView(path, viewType, options);
    if(view == null)
      throw new NoSuchFileException(path.toString());
    return type.cast(view.readAttributes());
  }

  /**
   * Reads a set of file attributes as a bulk operation. This method works in exactly the manner specified by the {@link
   * Files#readAttributes(Path,String,LinkOption[])} method.
   *
   * @param   path
   *          the path to the file
   * @param   attributes
   *          the attributes to read
   * @param   options
   *          unused argument
   *
   * @return  a map of the attributes returned; may be empty. The map's keys are the attribute names, its values are the
   *          attribute values
   *
   * @throws  NoSuchFileException
   *          if the target file does not exist
   * @throws  UnsupportedOperationException
   *          if the attribute view is not available
   * @throws  IllegalArgumentException
   *          if no attributes are specified or an unrecognized attributes is specified
   */
  @Nonnull
  @Override
  public Map<String, Object> readAttributes(@Nonnull Path path, @Nonnull String attributes, @Nonnull LinkOption... options) throws IOException {
    int viewNameEnd = attributes.indexOf(':');
    String viewName = viewNameEnd >= 0 ? attributes.substring(0, viewNameEnd) : GitFileAttributeView.Basic.BASIC_VIEW;
    String keys = viewNameEnd >= 0 ? attributes.substring(viewNameEnd + 1) : attributes;
    Class<? extends GitFileAttributeView> viewType;
    switch(viewName) {
      case GitFileAttributeView.Basic.BASIC_VIEW:
        viewType = GitFileAttributeView.Basic.class;
        break;
      case GitFileAttributeView.Posix.POSIX_VIEW:
        viewType = GitFileAttributeView.Posix.class;
        break;
      default:
        throw new UnsupportedOperationException("View \"" + viewName + "\" is not available");
    }
    GitFileAttributeView view = getFileAttributeView(path, viewType, options);
    if(view == null)
      throw new NoSuchFileException(path.toString());
    return view.readAttributes(Arrays.asList(keys.split(",")));
  }

  /**
   * Setting attribute is not supported with the current version.
   *
   * @throws UnsupportedOperationException whenever this method gets called
   */
  @Override
  public void setAttribute(@Nullable Path path, @Nullable String attribute, @Nullable Object value, @Nullable LinkOption... options) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

}
