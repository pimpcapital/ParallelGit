package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView;
import com.beijunyi.parallelgit.filesystem.io.GfsIO;
import com.beijunyi.parallelgit.filesystem.utils.GitGlobs;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitFileSystem extends FileSystem {

  private static final String GLOB_SYNTAX = "glob";
  private static final String REGEX_SYNTAX = "regex";
  private static final Set<String> SUPPORTED_VIEWS =
    Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                                                             GfsFileAttributeView.Basic.BASIC_VIEW,
                                                             GfsFileAttributeView.Posix.POSIX_VIEW
    )));

  private final GfsDataService ds;
  private final String session;
  private final GitFileStore store;
  private final GitPath rootPath;
  private String branch;
  private RevCommit commit;

  private String message;
  private RevCommit sourceCommit;

  private volatile boolean closed = false;

  public GitFileSystem(@Nonnull Repository repository, @Nonnull RevCommit commit, @Nullable String branch) throws IOException {
    this.branch = branch;
    this.commit = commit;
    ds = new GfsDataService(repository);
    rootPath = new GitPath(this, "/");
    session = UUID.randomUUID().toString();
    store = new GitFileStore(session, tree);
    GitFileSystemProvider.INSTANCE.register(this);
  }

  @Nonnull
  @Override
  public GitFileSystemProvider provider() {
    return GitFileSystemProvider.INSTANCE;
  }

  @Override
  public synchronized void close() {
    if(!closed) {
      closed = true;
      ds.close();
      GitFileSystemProvider.INSTANCE.unregister(this);
    }
  }

  @Override
  public synchronized boolean isOpen() {
    return !closed;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

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

  @Nullable
  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public WatchService newWatchService() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public String getSessionId() {
    return session;
  }

  @Nonnull
  public GitPath getRootPath() {
    return rootPath;
  }

  @Nonnull
  public GitFileStore getFileStore() {
    return store;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if(this == o)
      return true;

    if(o == null || getClass() != o.getClass())
      return false;

    GitFileSystem that = (GitFileSystem)o;
    return session.equals(that.session);

  }

  @Override
  public int hashCode() {
    return session.hashCode();
  }

  @Nonnull
  public AnyObjectId persist() throws IOException {
    AnyObjectId result = GfsIO.persistRoot(this);
    flush();
    return result;
  }

  @Nonnull
  public Repository getRepository() {
    return ds.getRepository();
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
  public String getMessage() {
    return message;
  }

  public void setMessage(@Nullable String message) {
    this.message = message;
  }

  @Nullable
  public RevCommit getSourceCommit() {
    return sourceCommit;
  }

  public void setSourceCommit(@Nullable RevCommit sourceCommit) {
    this.sourceCommit = sourceCommit;
  }

  @Nullable
  public AnyObjectId getTree() {
    return store.getTree();
  }

  public boolean isDirty() {
    return store.isDirty();
  }

}
