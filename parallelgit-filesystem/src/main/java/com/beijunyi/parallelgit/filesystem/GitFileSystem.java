package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.io.RootNode;
import com.beijunyi.parallelgit.filesystem.utils.GfsConfiguration;
import com.beijunyi.parallelgit.utils.RefUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView.Basic.BASIC_VIEW;
import static com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView.Posix.POSIX_VIEW;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.eclipse.jgit.lib.Constants.MASTER;

public class GitFileSystem extends FileSystem {

  public static final Set<String> SUPPORTED_VIEWS = unmodifiableSet(new HashSet<>(asList(BASIC_VIEW, POSIX_VIEW)));

  private final String sid;
  private final GfsObjectService objService;
  private final GfsFileStore fileStore;
  private final GfsStatusProvider statusProvider;

  private boolean closed = false;

  public GitFileSystem(GfsConfiguration cfg, String sid) throws IOException {
    this.sid = sid;
    objService = new GfsObjectService(cfg.repository());
    RevCommit commit = cfg.commit();
    String branch = cfg.branch();
    if(branch == null && commit == null)
      branch = RefUtils.fullBranchName(MASTER);
    fileStore = new GfsFileStore(commit, objService);
    statusProvider = new GfsStatusProvider(fileStore, branch, commit);
  }

  @Nonnull
  @Override
  public GitFileSystemProvider provider() {
    return GitFileSystemProvider.getDefault();
  }

  @Override
  public synchronized void close() {
    if(!closed) {
      closed = true;
      objService.close();
      statusProvider.close();
      GitFileSystemProvider.getDefault().unregister(this);
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
    return Collections.<Path>singleton(getRootPath());
  }

  @Nonnull
  @Override
  public Iterable<FileStore> getFileStores() {
    return Collections.<FileStore>singleton(fileStore);
  }

  @Nonnull
  @Override
  public Set<String> supportedFileAttributeViews() {
    return SUPPORTED_VIEWS;
  }

  @Nonnull
  @Override
  public GitPath getPath(String first, String... more) {
    String path;
    if(more.length == 0) {
      path = first;
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append(first);
      for(String segment: more) {
        if(segment.length() > 0) {
          if(sb.length() > 0) sb.append('/');
          sb.append(segment);
        }
      }
      path = sb.toString();
    }
    return new GitPath(this, path);
  }

  @Nonnull
  @Override
  public PathMatcher getPathMatcher(String syntaxAndInput) {
    return GfsPathMatcher.newMatcher(syntaxAndInput);
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
  public Repository getRepository() {
    return objService.getRepository();
  }

  @Nonnull
  public String getSessionId() {
    return sid;
  }

  @Nonnull
  public GitPath getRootPath() {
    return getPath("/");
  }

  @Nonnull
  public GfsObjectService getObjectService() {
    return objService;
  }

  @Nonnull
  public GfsFileStore getFileStore() {
    return fileStore;
  }

  @Nonnull
  public GfsStatusProvider getStatusProvider() {
    return statusProvider;
  }

  @Nonnull
  public ObjectId flush() throws IOException {
    RootNode root = fileStore.getRoot();
    ObjectId ret = root.getObjectId(true);
    objService.flush();
    return ret;
  }

  public void updateOrigin(ObjectId rootTree) throws IOException {
    RootNode root = fileStore.getRoot();
    root.updateOrigin(rootTree);
  }

  public void reset() throws IOException {
    RootNode root = fileStore.getRoot();
    root.reset();
  }

  @Override
  public boolean equals(@Nullable Object that) {
    return this == that
             || (that != null && getClass() == that.getClass() && sid.equals(((GitFileSystem)that).sid));

  }

  @Override
  public int hashCode() {
    return sid.hashCode();
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    close();
  }

}
