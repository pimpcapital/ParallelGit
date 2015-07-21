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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;

public class GitFileSystemProvider extends FileSystemProvider {

  public final static String GIT_FS_SCHEME = "gfs";

  public final static EnumSet<StandardOpenOption> SUPPORTED_OPEN_OPTIONS = EnumSet.of(READ, SPARSE, CREATE, CREATE_NEW, WRITE, APPEND, TRUNCATE_EXISTING);
  public final static EnumSet<StandardCopyOption> SUPPORTED_COPY_OPTIONS = EnumSet.of(REPLACE_EXISTING, ATOMIC_MOVE);

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
   * @throws  UnsupportedOperationException
   *          if an unsupported open option is specified
   */
  @Nonnull
  @Override
  public SeekableByteChannel newByteChannel(@Nonnull Path path, @Nonnull Set<? extends OpenOption> options, @Nonnull FileAttribute<?>... attrs) throws IOException {
    Set<OpenOption> unsupportedOperations = new HashSet<>(options);
    unsupportedOperations.removeAll(SUPPORTED_OPEN_OPTIONS);
    if(!unsupportedOperations.isEmpty())
      throw new UnsupportedOperationException(unsupportedOperations.toString());

    Set<OpenOption> amendedOptions = new HashSet<>(options);
    if(!options.contains(READ) && !options.contains(WRITE)) {
      if(options.contains(APPEND))
        amendedOptions.add(WRITE);
      else
        amendedOptions.add(READ);
    }

    GitPath gitPath = (GitPath) path;
    GitFileStore store = gitPath.getFileSystem().getFileStore();

    return store.newByteChannel(gitPath.getNormalizedString(), amendedOptions);
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
    GitPath gitPath = (GitPath) path;
    GitFileStore store = gitPath.getFileSystem().getFileStore();
    return store.newDirectoryStream(gitPath.getNormalizedString(), filter);
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
  public void createDirectory(@Nonnull Path dir, @Nullable FileAttribute<?>... attrs) throws IOException {
    GitPath gitPath = (GitPath) dir;
    GitFileStore store = gitPath.getFileSystem().getFileStore();
    if(store.fileExists(gitPath.getNormalizedString()) || store.isDirectory(gitPath.getNormalizedString()))
      throw new FileAlreadyExistsException(dir.toString());
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
    GitPath gitPath = (GitPath) path;
    GitFileStore store = gitPath.getFileSystem().getFileStore();
    store.delete(gitPath.getNormalizedString());
  }

  /**
   * Checks if the given array contains a {@code CopyOption} that is not {@link #SUPPORTED_COPY_OPTIONS supported} by
   * {@code GitFileSystem}.
   *
   * @param   options
   *          the array of {@code CopyOption}s to check
   * @return  the given {@code CopyOption}s in a {@code Set}
   * @throws  UnsupportedOperationException
   *          if the given array contains an unsupported {@code CopyOption}
   */
  @Nonnull
  private Set<? extends CopyOption> checkCopyOptions(@Nonnull CopyOption... options) throws UnsupportedOperationException {
    Set<? extends CopyOption> supportedOptions = new HashSet<>(SUPPORTED_COPY_OPTIONS);
    Set<CopyOption> requestedOptions = new HashSet<>(Arrays.asList(options));
    Set<CopyOption> failedOptions = new HashSet<>(requestedOptions);
    failedOptions.removeAll(supportedOptions);
    if(!failedOptions.isEmpty())
      throw new UnsupportedOperationException(failedOptions.toString());

    return requestedOptions;
  }

  /**
   * Tests if the two {@code GitPath}s are created from {@code GitFileSystem}s that are based on the same git
   * repository.
   *
   * @param  source
   *         one path to test
   * @param  target
   *         the other path to test
   * @return {@code true} if the given paths are created from {@code GitFileSystem}s that are based on the same git
   *         repository.
   */
  private static boolean useSameRepository(@Nonnull GitPath source, @Nonnull GitPath target) {
    Repository srcRepo = source.getFileSystem().getFileStore().getRepository();
    Repository targetRepo = target.getFileSystem().getFileStore().getRepository();
    return srcRepo.getDirectory().equals(targetRepo.getDirectory());
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
   * @throws  UnsupportedOperationException
   *          if {@code options} contains a {@code CopyOption} that is not {@link #SUPPORTED_COPY_OPTIONS supported}
   */
  @Override
  public void copy(@Nonnull Path source, @Nonnull Path target, @Nonnull CopyOption... options) throws IOException, UnsupportedOperationException {
    GitPath sourcePath = (GitPath) source;
    GitPath targetPath = (GitPath) target;
    if(!useSameRepository(sourcePath, targetPath)) {
      Files.copy(newInputStream(sourcePath), targetPath, options);
      return;
    }

    GitFileSystem sourceFs = sourcePath.getFileSystem();
    GitFileStore sourceStore = sourceFs.getFileStore();
    GitFileSystem targetFs = targetPath.getFileSystem();
    boolean replaceExisting = checkCopyOptions(options).contains(REPLACE_EXISTING);
    if(sourceFs.equals(targetFs))
      sourceStore.copy(sourcePath.getNormalizedString(), targetPath.getNormalizedString(), replaceExisting);
    else {
      if(sourceStore.isDirty(sourcePath.getNormalizedString()))
        Files.copy(newInputStream(sourcePath), targetPath, options);
      else {
        ObjectId blobId = sourceStore.getFileBlobId(sourcePath.getNormalizedString());
        if(blobId != null)
          targetFs.getFileStore().createFileFromBlob(targetPath.getNormalizedString(), blobId, replaceExisting);
      }
    }
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
   * @throws  UnsupportedOperationException
   *          if {@code options} contains a {@code CopyOption} that is not {@link #SUPPORTED_COPY_OPTIONS supported}
   */
  @Override
  public void move(@Nonnull Path source, @Nonnull Path target, @Nonnull CopyOption... options) throws IOException, UnsupportedOperationException {
    GitPath sourcePath = (GitPath) source;
    GitPath targetPath = (GitPath) target;
    GitFileSystem sourceFs = sourcePath.getFileSystem();
    GitFileStore sourceStore = sourceFs.getFileStore();
    GitFileSystem targetFs = targetPath.getFileSystem();
    if(!useSameRepository(sourcePath, targetPath) || !sourceFs.equals(targetFs)) {
      if(sourceStore.fileExists(sourcePath.getNormalizedString())) {
        copy(sourcePath, targetPath, options);
        delete(sourcePath);
      } else if(sourceStore.isDirectory(sourcePath.getNormalizedString()))
        throw new DirectoryNotEmptyException(source.relativize(sourceFs.getRoot()).toString());
      return;
    }
    GitFileStore store = sourcePath.getFileSystem().getFileStore();
    store.move(sourcePath.getNormalizedString(), targetPath.getNormalizedString(), checkCopyOptions(options).contains(REPLACE_EXISTING));
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
  public GitFileStore getFileStore(@Nonnull Path path)  {
    GitPath gitPath = (GitPath) path;
    return gitPath.getFileSystem().getFileStore();
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
    GitPath gitPath = (GitPath) path;
    GitFileStore store = gitPath.getFileSystem().getFileStore();
    if(!store.fileExists(gitPath.getNormalizedString()) && !store.isDirectory(gitPath.getNormalizedString()))
      throw new NoSuchFileException(gitPath.toString());

    for(AccessMode mode : modes) {
      if(mode == AccessMode.EXECUTE && !store.isExecutableFile(gitPath.getNormalizedString()))
        throw new AccessDeniedException(path.toString());
    }
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
  @Nonnull
  @Override
  public <V extends FileAttributeView> V getFileAttributeView(@Nonnull Path path, @Nonnull Class<V> type, @Nonnull LinkOption... options) throws UnsupportedOperationException {
    GitPath gitPath = (GitPath) path;
    GitFileStore store = gitPath.getFileSystem().getFileStore();
    if(type == BasicFileAttributeView.class)
      return type.cast(new GitFileAttributeView.Basic(store, gitPath.getNormalizedString()));
    if(type == PosixFileAttributeView.class)
      return type.cast(new GitFileAttributeView.Posix(store, gitPath.getNormalizedString()));
    throw new UnsupportedOperationException(type.getName());
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
    Class<? extends BasicFileAttributeView> view;
    if(type == BasicFileAttributes.class)
      view = BasicFileAttributeView.class;
    else if(type == PosixFileAttributes.class)
      view = PosixFileAttributeView.class;
    else
      throw new UnsupportedOperationException(type.getName());
    return type.cast(getFileAttributeView(path, view, options).readAttributes());
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
    String keys = viewNameEnd >= 0 ? attributes.substring(viewNameEnd) : attributes;
    Class<? extends GitFileAttributeView> viewClass;
    switch(viewName) {
      case GitFileAttributeView.Basic.BASIC_VIEW:
        viewClass = GitFileAttributeView.Basic.class;
        break;
      case GitFileAttributeView.Posix.POSIX_VIEW:
        viewClass = GitFileAttributeView.Posix.class;
        break;
      default:
        throw new UnsupportedOperationException("View \"" + viewName + "\" is not available");
    }
    return getFileAttributeView(path, viewClass, options).readAttributes(Arrays.asList(keys.split(",")));
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
