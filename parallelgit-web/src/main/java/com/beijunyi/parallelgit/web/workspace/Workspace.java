package com.beijunyi.parallelgit.web.workspace;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.BranchUtils;
import org.eclipse.jgit.lib.Repository;

public class Workspace implements Closeable {

  private final String id;
  private final GitUser user;
  private final Repository repo;

  private GitFileSystem gfs;

  public Workspace(@Nonnull String id, @Nonnull GitUser user, @Nonnull Repository repo) {
    this.id = id;
    this.user = user;
    this.repo = repo;
  }

  @Nonnull
  public Object getData(@Nonnull DataRequest request) throws IOException {
    switch(request.getType()) {
      case "branches":
        return getBranches();
      default:
        throw new UnsupportedOperationException(request.getType());
    }
  }

  @Nonnull
  public Map<String, Object> updateData(@Nonnull DataUpdate update) throws IOException {
    Map<String, Object> updated = new HashMap<>();
    switch(update.getType()) {
      case "checkout":
        checkout(update.getValue());
        updated.put("head", getHead());
        break;
      default:
        throw new UnsupportedOperationException();
    }
    return updated;
  }

  @Nonnull
  public SortedSet<String> getBranches() throws IOException {
    return new TreeSet<>(BranchUtils.getBranches(repo).keySet());
  }

  @Nonnull
  public String getHead() {
    if(gfs == null)
      throw new IllegalStateException();
    GfsStatusProvider status = gfs.getStatusProvider();
    if(status.isAttached())
      return status.branch();
    if(status.isInitialized())
      return status.commit().name();
    throw new IllegalStateException();
  }

  public void checkout(@Nonnull String branch) throws IOException {
    if(gfs == null) {
      gfs = Gfs.newFileSystem(branch, repo);
      return;
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws IOException {
    if(gfs != null)
      gfs.close();
  }

}
