package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.utils.GitGlobs;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitFileSystem extends FileSystem {

  private static final String GLOB_SYNTAX = "glob";
  private static final String REGEX_SYNTAX = "regex";
  private static final Set<String> SUPPORTED_VIEWS =
    Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                                                             GitFileAttributeView.Basic.BASIC_VIEW,
                                                             GitFileAttributeView.Posix.POSIX_VIEW
    )));

  private final GitFileSystemProvider provider;
  private final Repository repository;
  private final String session;
  private final GitFileStore store;
  private final GitPath rootPath;
  private String branch;
  private RevCommit commit;

  private ObjectReader reader;
  private boolean closed = false;

  public GitFileSystem(@Nonnull GitFileSystemProvider provider, @Nonnull Repository repository, @Nullable String branch, @Nullable RevCommit commit, @Nullable AnyObjectId tree) throws IOException {
    this.provider = provider;
    this.repository = repository;
    this.session = UUID.randomUUID().toString();
    this.rootPath = new GitPath(this, "/");
    this.branch = branch;
    this.commit = commit;
    store = new GitFileStore(repository, rootPath, tree);
  }

  /**
   * Returns the provider that created this file system.
   *
   * @return  the provider that created this file system.
   */
  @Nonnull
  @Override
  public GitFileSystemProvider provider() {
    return provider;
  }

  /**
   * Closes this file system.
   *
   * After a file system is closed then all subsequent access to the file system, either by methods defined by this
   * class or on objects associated with this file system, throw {@link ClosedFileSystemException}. If the file system
   * is already closed then invoking this method has no effect.
   *
   * Closing a file system will close all open {@link java.nio.channels.Channel}, {@link DirectoryStream}, and other
   * closeable objects associated with this file system.
   */
  @Override
  public synchronized void close() {
    if(!closed) {
      closed = true;
      provider.unregister(this);
    }
  }

  /**
   * Tells whether or not this file system is open.
   *
   * @return {@code true} if this file system is open
   */
  @Override
  public synchronized boolean isOpen() {
    return !closed;
  }

  /**
   * Returns {@code false} as {@code GitFileSystem} provides both read access and write access.
   *
   * @return {@code false}
   */
  @Override
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns "/" as it is the only separator {@code GitFileSystem} uses regardless the operating system the JVM runs on.
   *
   * @return "/"
   */
  @Nonnull
  @Override
  public String getSeparator() {
    return "/";
  }

  @Nonnull
  @Override
  public Iterable<Path> getRootDirectories() {
    final List<Path> allowedList = Collections.<Path>singletonList(rootPath);
    return new Iterable<Path>() {
      @Override
      public Iterator<Path> iterator() {
        return allowedList.iterator();
      }
    };
  }

  @Nonnull
  @Override
  public Iterable<FileStore> getFileStores() {
    final List<FileStore> allowedList = Collections.<FileStore>singletonList(store);
    return new Iterable<FileStore>() {
      @Override
      public Iterator<FileStore> iterator() {
        return allowedList.iterator();
      }
    };
  }

  @Nonnull
  @Override
  public Set<String> supportedFileAttributeViews() {
    return SUPPORTED_VIEWS;
  }

  @Nonnull
  @Override
  public GitPath getPath(@Nonnull String first, @Nonnull String... more) {
    String path;
    if(more.length == 0)
      path = first;
    else {
      StringBuilder sb = new StringBuilder();
      sb.append(first);
      for(String segment: more) {
        if(segment.length() > 0) {
          if(sb.length() > 0)
            sb.append('/');
          sb.append(segment);
        }
      }
      path = sb.toString();
    }
    return new GitPath(this, path);
  }

  @Nonnull
  @Override
  public PathMatcher getPathMatcher(@Nonnull String syntaxAndInput) {
    int pos = syntaxAndInput.indexOf(':');
    if(pos <= 0 || pos == syntaxAndInput.length())
      throw new IllegalArgumentException();

    String syntax = syntaxAndInput.substring(0, pos);
    String input = syntaxAndInput.substring(pos + 1);

    String expr;
    if(syntax.equals(GLOB_SYNTAX))
      expr = GitGlobs.toRegexPattern(input);
    else {
      if(syntax.equals(REGEX_SYNTAX))
        expr = input;
      else
        throw new UnsupportedOperationException("Syntax '" + syntax + "' not recognized");
    }

    final Pattern pattern = Pattern.compile(expr);

    return new PathMatcher() {
      @Override
      public boolean matches(@Nonnull Path path) {
        return pattern.matcher(path.toString()).matches();
      }
    };
  }

  /**
   * {@code UserPrincipalLookupService} is not supported with the current version.
   *
   * @throws UnsupportedOperationException whenever this method gets called
   */
  @Nullable
  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@code WatchService} is not supported with the current version.
   *
   * @throws UnsupportedOperationException whenever this method gets called
   */
  @Nullable
  @Override
  public WatchService newWatchService() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the session id as the unique identifier of this {@code FileSystem}.
   *
   * @return the session id of this {@code FileSystem}
   */
  @Nonnull
  public String getSessionId() {
    return session;
  }

  /**
   * Returns the root path of this file system.
   *
   * @return the root path of this file system
   */
  @Nonnull
  public GitPath getRootPath() {
    return rootPath;
  }

  /**
   * Returns the file store of this file system.
   *
   * @return the file store of this file system
   */
  @Nonnull
  public GitFileStore getFileStore() {
    return store;
  }

  /**
   * Tests whether the given object is equal to this instance.
   *
   * @param o an object
   * @return {@code true} if the given object is equal to this instance
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if(this == o)
      return true;

    if(o == null || getClass() != o.getClass())
      return false;

    GitFileSystem that = (GitFileSystem)o;
    return session.equals(that.session);

  }

  /**
   * Returns the hash code of this instance.
   *
   * The hash code of a git file system is the hash code of its session id.
   *
   * @return the hash code of this instance
   */
  @Override
  public int hashCode() {
    return session.hashCode();
  }

  @Nonnull
  public byte[] loadObject(@Nonnull AnyObjectId objectId) throws IOException {
    if(reader == null)
      reader = repository.newObjectReader();
    return reader.open(objectId).getBytes();
  }


  @Nonnull
  public Repository getRepository() {
    return repository;
  }

  @Nullable
  public String getBranch() {
    return branch;
  }

  public void setBranch(@Nullable String branch) {
    this.branch = branch;
  }

  @Nullable
  public RevCommit getCommit() {
    return commit;
  }

  public void setCommit(@Nullable RevCommit commit) {
    this.commit = commit;
  }

  @Nullable
  public AnyObjectId getTree() {
    return store.getTree();
  }

}
