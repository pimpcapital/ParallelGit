package com.beijunyi.parallelgit.gfs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.RefHelper;
import com.beijunyi.parallelgit.utils.RepositoryHelper;
import org.eclipse.jgit.lib.*;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardCopyOption.*;

public class GitFileSystemProvider extends FileSystemProvider {

  public final static String GIT_FS_SCHEME = "gfs";
  public final static String ROOT_SEPARATOR = "!";

  public final static String SESSION_KEY = "session";
  public final static String BARE_KEY = "bare";
  public final static String CREATE_KEY = "create";
  public final static String BRANCH_KEY = "branch";
  public final static String REVISION_KEY = "revision";
  public final static String TREE_KEY = "tree";

  public final static EnumSet<StandardOpenOption> SUPPORTED_OPEN_OPTIONS = EnumSet.of(READ, SPARSE, CREATE, CREATE_NEW, WRITE, APPEND, TRUNCATE_EXISTING);
  public final static EnumSet<StandardCopyOption> SUPPORTED_COPY_OPTIONS = EnumSet.of(REPLACE_EXISTING, ATOMIC_MOVE);

  private final Map<String, GitFileSystem> gfsMap = new HashMap<>();

  private static GitFileSystemProvider INSTANCE;

  /**
   * Returns the {@link FileSystemProvider#installedProviders() installed} {@code GitFileSystemProvider} instance or
   * throws a {@code ProviderNotFoundException} if none is installed.
   *
   * @return  the installed {@code GitFileSystemProvider} instance
   */
  @Nonnull
  static GitFileSystemProvider getInstance() {
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
   * Returns {@code "git"} as the scheme of this provider.
   *
   * @return  {@code "git"}
   */
  @Nonnull
  @Override
  public String getScheme() {
    return GIT_FS_SCHEME;
  }

  /**
   * Creates a new {@code GitFileSystem} from a repository directory. The configuration parameters are the same as for
   * {@link #newFileSystem(URI,Map)}()}.
   *
   * @param   repoDir
   *          the directory of the repository that the new {@code GitFileSystem} bases on
   * @param   env
   *          A map of provider specific properties to configure the file system
   * @return  the result {@code GitFileSystem}
   *
   * @throws  FileSystemAlreadyExistsException
   *          if a session ID is specified and a {@code GitFileSystem} with this session ID has already been created
   */
  @Nonnull
  GitFileSystem newFileSystem(@Nonnull File repoDir, @Nullable Map<String, ?> env) throws FileSystemAlreadyExistsException {
    String session = null;
    boolean bare = false;
    boolean create = false;
    String branch = null;
    String revisionStr = null;
    String treeStr = null;

    if(env != null) {
      Object sessionValue = env.get(SESSION_KEY);
      session = sessionValue == null ? null : sessionValue.toString();
      Object bareValue = env.get(BARE_KEY);
      bare = bareValue instanceof String ? Boolean.valueOf((String) bareValue) : Boolean.TRUE.equals(bareValue);
      Object createValue = env.get(CREATE_KEY);
      create = createValue instanceof String ? Boolean.valueOf((String) createValue) : Boolean.TRUE.equals(createValue);
      Object branchValue = env.get(BRANCH_KEY);
      branch = branchValue == null ? null : branchValue instanceof Ref ? ((Ref) branchValue).getName() : branchValue.toString();
      Object revisionValue = env.get(REVISION_KEY);
      revisionStr = revisionValue == null ? null : revisionValue instanceof AnyObjectId ? ((AnyObjectId) revisionValue).getName() : revisionValue.toString();
      Object treeValue = env.get(TREE_KEY);
      treeStr = treeValue == null ? null : treeValue instanceof AnyObjectId ? ((AnyObjectId) treeValue).getName() : treeValue.toString();
    }

    Repository repo = create ? RepositoryHelper.newRepository(repoDir, bare) : RepositoryHelper.openRepository(repoDir, bare);
    ObjectId revision = revisionStr != null ? RepositoryHelper.getRevisionId(repo, revisionStr) : null;
    ObjectId tree = treeStr != null ? RepositoryHelper.getRevisionId(repo, treeStr) : null;

    return newFileSystem(session, repo, branch, revision, tree);
  }

  @Nonnull
  @Override
  public GitFileSystem newFileSystem(@Nonnull Path path, @Nullable Map<String, ?> env) {
    return newFileSystem(path.toFile(), env);
  }

  /**
   * Constructs a new {@code FileSystem} object identified by a {@code URI}. This method is invoked by the {@link
   * FileSystems#newFileSystem(URI,Map)} method to open a new file system identified by a {@code URI}.
   *
   * The {@code uri} parameter is an absolute, hierarchical {@code URI}, with a scheme equal (without regard to case) to
   * {@link #GIT_FS_SCHEME}. The syntax should match this pattern:
   *
   * git://[repo location]![file in repo (optional)]?[parameters (optional)]
   *
   * The value of {@code repo location} indicates the location of the repository.
   *
   * The value of {@code file in repo} indicates the file in the repository to be loaded, which has little meaning to
   * this method. However, this value is important when creating a {@code GitPath} from a {@code URI}.
   *
   * The {@code parameters} part can be used to specified the these optional configurations:
   * {@link #SESSION_KEY session}    the unique identifier of the new {@code GitFileSystem}, which must not be the same
   *                                 as any existing session IDs. If this parameter is absent, a random session ID will
   *                                 be generated.
   * {@link #BARE_KEY bare}          indicates whether the new {@code GitFileSystem} is based on a bare repository. If
   *                                 this parameter is absent, the default value to this configuration is {@code false}.
   * {@link #CREATE_KEY create}      indicates whether to initialize a new repository when creating the new {@code
   *                                 GitFileSystem}. If this parameter is absent, the default value to this
   *                                 configuration is {@code false}.
   * {@link #BRANCH_KEY branch}      the branch that the new {@code GitFileSystem} is attached to. If this parameter is
   *                                 absent, the result {@code GitFileSystem} is detached. Committing changes to a
   *                                 detached {@code GitFileSystem} will not update the {@code HEAD} of any branch.
   * {@link #REVISION_KEY revision}  the revision to the commit that the new {@code GitFileSystem} is based on.
   *                                 Committing changes to this {@code GitFileSystem} will result in a new commit whose
   *                                 parent is this commit. Amending commit will result in a new commit whose parent is
   *                                 this commit's parent. If this parameter is absent, the default base commit is the
   *                                 {@code HEAD} of the attached branch, or {@code null} if no branch is specified.
   * {@link #TREE_KEY tree}          the root of the file tree to be loaded into the new {@code GitFileSystem}. If this
   *                                 parameter is absent, the default tree is the tree of the base commit, or {@code
   *                                 null} if no commit is specified, in which case, the new {@code GitFileSystem} will
   *                                 have no content.
   *
   * The {@code env} parameter is a map of provider specific properties to configure the file system. All of the
   * configuration parameters above can also be specified in {@code env}.
   *
   * This method throws {@link FileSystemAlreadyExistsException} if the file system with the specified session id has
   * already been created.
   *
   * @param   uri
   *          {@code URI} reference
   * @param   env
   *          A map of provider specific properties to configure the file system
   * @return  the result {@code GitFileSystem}
   *
   * @throws  FileSystemAlreadyExistsException
   *          if a session ID is specified and a {@code GitFileSystem} with this session ID has already been created
   */
  @Nonnull
  @Override
  public GitFileSystem newFileSystem(@Nonnull URI uri, @Nullable Map<String, ?> env) {
    Map<String, Object> params = GitUriUtils.getParams(uri);
    if(env != null)
      params.putAll(env);
    File repoDir = new File(GitUriUtils.getRepoPath(uri));
    return newFileSystem(repoDir, params);
  }

  /**
   * Creates a new {@code GitFileSystem} from a repository.
   *
   * @param   sessionId
   *          the unique identifier of the new {@code GitFileSystem}, which must not be the same as any existing session
   *          IDs. If this parameter is {@code null}, a random session ID will be generated.
   * @param   repo
   *          the repository that the new {@code GitFileSystem} is based on.
   * @param   branch
   *          the branch that the new {@code GitFileSystem} is attached to. If this parameter is {@code null}, the
   *          result {@code GitFileSystem} is detached. Committing changes to a detached {@code GitFileSystem} will not
   *          update the {@code HEAD} of any branch.
   * @param   revision
   *          the revision to the commit that the new {@code GitFileSystem} is based on. Committing changes to this {
   *          @code GitFileSystem} will result in a new commit whose parent is this commit. Amending commit will result
   *          in a new commit whose parent is this commit's parent. If this parameter is {@code null}, the default base
   *          commit is the {@code HEAD} of the attached branch, or {@code null} if no branch is specified.
   * @param   tree
   *          the root of the file tree to be loaded into the new {@code GitFileSystem}. If this parameter is {@code
   *          null}, the default tree is the tree of the base commit, or {@code null} if no commit is specified, in
   *          which case, the new {@code GitFileSystem} will have no content.
   * @return  the result {@code GitFileSystem}
   *
   * @throws  FileSystemAlreadyExistsException
   *          if a session ID is specified and a {@code GitFileSystem} with this session ID has already been created
   */
  @Nonnull
  GitFileSystem newFileSystem(@Nullable String sessionId, @Nonnull Repository repo, @Nullable String branch, @Nullable ObjectId revision, @Nullable ObjectId tree) throws FileSystemAlreadyExistsException {
    String branchRef = branch != null ? RefHelper.getBranchRefName(branch) : null;
    if(revision == null && branchRef != null)
      revision = RepositoryHelper.getRevisionId(repo, branchRef);
    GitFileSystem gfs = new GitFileSystem(this, checkSessionId(sessionId), repo, branchRef, revision, tree);
    gfsMap.put(gfs.getSessionId(), gfs);
    return gfs;
  }

  /**
   * Checks if a {@code GitFileSystem} with the given session ID already exists or generate and return a unique string
   * ID if the given value is {@code null}.
   *
   * @param   sessionId
   *          the session ID to check
   * @return  the given session ID or a new string ID if the given value is {@code null}
   *
   * @throws  FileSystemAlreadyExistsException
   *          if a {@code GitFileSystem} with the given session ID already exists
   */
  @Nonnull
  private String checkSessionId(@Nullable String sessionId) throws FileSystemAlreadyExistsException {
    if(sessionId != null) {
      if(gfsMap.containsKey(sessionId))
        throw new FileSystemAlreadyExistsException(sessionId);
    } else
      sessionId = UUID.randomUUID().toString();
    return sessionId;
  }

  public void unregister(@Nonnull GitFileSystem gfs) {
    gfsMap.remove(gfs.getSessionId());
  }

  @Nullable
  GitFileSystem getFileSystem(@Nonnull String sessionId) {
    return gfsMap.get(sessionId);
  }

  @Nullable
  @Override
  public GitFileSystem getFileSystem(@Nonnull URI uri) {
    Map<String, Object> params = GitUriUtils.getParams(uri);
    String idValue = (String) params.get(SESSION_KEY);
    return getFileSystem(idValue);
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
  public GitPath getPath(@Nonnull URI uri) throws ProviderMismatchException {
    GitFileSystem gfs = getFileSystem(uri);
    if(gfs == null)
      gfs = newFileSystem(uri, null);
    String fileInRepo = GitUriUtils.getFileInRepo(uri);
    return gfs.getPath(fileInRepo).toRealPath();
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
  public SeekableByteChannel newByteChannel(@Nonnull Path path, @Nonnull Set<? extends OpenOption> options, @Nonnull FileAttribute<?>... attrs) throws NoSuchFileException, AccessDeniedException, FileAlreadyExistsException, UnsupportedOperationException {
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
  public GitDirectoryStream newDirectoryStream(@Nonnull Path path, @Nullable DirectoryStream.Filter<? super Path> filter) throws NotDirectoryException {
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
  public void createDirectory(@Nonnull Path dir, @Nullable FileAttribute<?>... attrs) throws FileAlreadyExistsException {
    GitPath gitPath = (GitPath) dir;
    GitFileStore store = gitPath.getFileSystem().getFileStore();
    if(store.isRegularFile(gitPath.getNormalizedString()) || store.isDirectory(gitPath.getNormalizedString()))
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
  public void delete(@Nonnull Path path) throws AccessDeniedException, DirectoryNotEmptyException, NoSuchFileException {
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
          targetFs.getFileStore().createFileWithObjectId(targetPath.getNormalizedString(), blobId, replaceExisting);
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
      if(sourceStore.isRegularFile(sourcePath.getNormalizedString())) {
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
  public void checkAccess(@Nonnull Path path, @Nonnull AccessMode... modes) throws NoSuchFileException, AccessDeniedException {
    GitPath gitPath = (GitPath) path;
    GitFileStore store = gitPath.getFileSystem().getFileStore();
    if(!store.isRegularFile(gitPath.getNormalizedString()) && !store.isDirectory(gitPath.getNormalizedString()))
      throw new NoSuchFileException(gitPath.toString());

    for(AccessMode mode : modes) {
      if(mode == AccessMode.EXECUTE)
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
    if(!type.isAssignableFrom(GitFileAttributeView.class))
      throw new UnsupportedOperationException(type.getName());
    GitPath gitPath = (GitPath) path;
    GitFileStore store = gitPath.getFileSystem().getFileStore();
    return type.cast(new GitFileAttributeView(store, gitPath.getNormalizedString()));
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
  public <A extends BasicFileAttributes> A readAttributes(@Nonnull Path path, @Nonnull Class<A> type, @Nonnull LinkOption... options) throws NoSuchFileException, UnsupportedOperationException {
    if(!type.isAssignableFrom(GitFileAttributes.class))
      throw new UnsupportedOperationException(type.getName());
    return type.cast(getFileAttributeView(path, GitFileAttributeView.class, options).readAttributes());
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
  public Map<String, Object> readAttributes(@Nonnull Path path, @Nonnull String attributes, @Nonnull LinkOption... options) throws NoSuchFileException, UnsupportedOperationException, IllegalArgumentException {
    int viewNameEnd = attributes.indexOf(':');
    if(viewNameEnd != -1) {
      String viewName = attributes.substring(0, viewNameEnd);
      if(!GitFileAttributeView.GIT_FILE_ATTRIBUTE_VIEW_TYPE.equals(viewName))
        throw new UnsupportedOperationException(viewName);
      attributes = attributes.substring(viewNameEnd + 1);
    }
    return getFileAttributeView(path, GitFileAttributeView.class, options).readAttributes(attributes);
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
