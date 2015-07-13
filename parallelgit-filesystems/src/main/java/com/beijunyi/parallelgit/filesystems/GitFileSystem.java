package com.beijunyi.parallelgit.filesystems;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitFileSystem extends FileSystem {

  private static final String GLOB_SYNTAX = "glob";
  private static final String REGEX_SYNTAX = "regex";
  private static final Set<String> SUPPORTED_VIEWS = Collections.singleton(GitFileAttributeView.GIT_FILE_ATTRIBUTE_VIEW_TYPE);

  private final GitFileSystemProvider provider;
  private final String session;
  private final GitPath root;
  private final GitFileStore store;

  private boolean closed = false;

  GitFileSystem(@Nonnull GitFileSystemProvider provider, @Nonnull Repository repo, @Nullable String branch, @Nullable AnyObjectId baseCommit, @Nullable AnyObjectId baseTree) throws IOException {
    this.provider = provider;
    this.root = new GitPath(this, "/");
    session = UUID.randomUUID().toString();
    store = new GitFileStore(root, repo, branch, baseCommit, baseTree);
  }

  /**
   * Returns the provider that created this file system.
   *
   * @return The provider that created this file system.
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
  public void close() {
    synchronized(this) {
      if(!closed) {
        closed = true;
        provider.unregister(this);
        store.close();
      }
    }
  }

  /**
   * Tells whether or not this file system is open.
   *
   * @return {@code true} if this file system is open
   */
  @Override
  public boolean isOpen() {
    synchronized(this) {
      return !closed;
    }
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
    final List<Path> allowedList = Collections.singletonList((Path)root);
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
    final List<FileStore> allowedList = Collections.singletonList((FileStore)store);
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
  public GitPath getRoot() {
    return root;
  }

  /**
   * Returns the file store of this file system.
   *
   * @return the file store of this file system
   */
  @Nonnull
  GitFileStore getFileStore() {
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
  public Repository getRepository() {
    return store.getRepository();
  }

  @Nullable
  public String getBranch() {
    return store.getBranch();
  }

  @Nullable
  public RevCommit getBaseCommit() {
    return store.getBaseCommit();
  }

  @Nullable
  public AnyObjectId getBaseTree() {
    return store.getBaseTree();
  }

}
