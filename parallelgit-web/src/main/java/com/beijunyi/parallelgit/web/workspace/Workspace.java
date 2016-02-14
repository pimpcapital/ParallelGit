package com.beijunyi.parallelgit.web.workspace;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.web.protocol.model.FileAttributes;
import com.beijunyi.parallelgit.web.workspace.status.Head;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

public class Workspace {

  private final String id;
  private final WorkspaceManager workspaceManager;

  private User user;
  private Repository repo;
  private GitFileSystem gfs;

  public Workspace(@Nonnull String id, @Nonnull WorkspaceManager workspaceManager) {
    this.id = id;
    this.workspaceManager = workspaceManager;
  }

  public void init(@Nonnull User user, @Nonnull Repository repo) throws IOException {
    this.user = user;
    this.repo = repo;
    gfs = Gfs.newFileSystem(getDefaultBranch(repo), repo);
  }

  @Nonnull
  public WorkspaceStatus getStatus() {
    return new WorkspaceStatus(Head.of(gfs));
  }

  @Nonnull
  public Repository getRepo() {
    if(repo == null)
      throw new IllegalStateException();
    return repo;
  }

  public void setRepo(@Nonnull Repository repo) {
    if(this.repo != null)
      throw new IllegalStateException();
    this.repo = repo;
  }

  public void setUser(@Nonnull User user) {
    this.user = user;
  }

  public boolean isInitialized() {
    return gfs != null;
  }

  @Nonnull
  public GitFileSystem getFileSystem() {
    if(gfs == null)
      throw new IllegalStateException();
    return gfs;
  }

  public void setFileSystem(@Nonnull GitFileSystem gfs) {
    if(this.gfs != null)
      throw new IllegalStateException();
    this.gfs = gfs;
  }

  @Nonnull
  public Head getHead() throws IOException {
    checkFS();
    return Head.of(gfs);
  }

  @Nonnull
  public SortedSet<String> getBranches() throws IOException {
    return new TreeSet<>(BranchUtils.getBranches(repo).keySet());
  }

  @Nonnull
  public String getFile(@Nonnull WorkspaceRequest request) throws IOException {
    checkFS();
    String path = request.getTarget();
    if(path == null)
      throw new IllegalStateException();
    return new String(Files.readAllBytes(gfs.getPath(path)));
  }

  @Nonnull
  public FileAttributes getFileAttributes(@Nonnull WorkspaceRequest request) throws IOException {
    return FileAttributes.read(getGitPath(request));
  }

  @Nonnull
  public CheckoutResult checkout(@Nonnull WorkspaceRequest request) throws IOException {
    String branch = request.getValue();
    if(branch == null)
      throw new IllegalStateException();
    if(gfs == null) {
      gfs = Gfs.newFileSystem(branch, repo);
      return CheckoutResult.success();
    }
    return CheckoutResult.wrap(Gfs.checkout(gfs).setTarget(branch).execute());
  }

  @Nonnull
  public FileAttributes save(@Nonnull WorkspaceRequest request) throws IOException {
    checkFS();
    String path = request.getTarget();
    String data = request.getValue();
    if(path == null || data == null)
      throw new IllegalStateException();
    Path file = gfs.getPath(path);
    Files.write(file, data.getBytes());
    return FileAttributes.read(file);
  }

  public void destroy() throws IOException {
    if(gfs != null)
      gfs.close();
    workspaceManager.destroyWorkspace(id);
  }

  private void checkFS() {
    if(gfs == null)
      throw new IllegalStateException();
  }

  @Nonnull
  private Path getGitPath(@Nonnull WorkspaceRequest request) {
    checkFS();
    String path = request.getTarget();
    if(path == null)
      throw new IllegalStateException();
    return gfs.getPath(path);
  }

  @Nonnull
  private static String getDefaultBranch(@Nonnull Repository repo) throws IOException {
    if(BranchUtils.branchExists(Constants.MASTER, repo))
      return Constants.MASTER;
    List<String> branches = new ArrayList<>(BranchUtils.getBranches(repo).keySet());
    if(branches.isEmpty())
      return Constants.MASTER;
    Collections.sort(branches);
    return branches.get(0);
  }

}
