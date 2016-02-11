package com.beijunyi.parallelgit.web.workspace;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.web.data.FileAttributes;
import com.beijunyi.parallelgit.web.data.Head;
import org.eclipse.jgit.lib.Repository;

public class Workspace {

  private final String id;
  private final WorkspaceManager workspaceManager;

  private Repository repo;
  private User user;

  private GitFileSystem gfs;

  public Workspace(@Nonnull String id, @Nonnull WorkspaceManager workspaceManager) {
    this.id = id;
    this.workspaceManager = workspaceManager;
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
  public Object processRequest(@Nonnull WorkspaceRequest request) throws IOException {
    switch(request.getType()) {
      case "head":
        return getHead();
      case "branches":
        return getBranches();
      case "list-children":
        return listChildren(request);
      case "file":
        return getFile(request);
      case "get-file-attributes":
        return getFileAttributes(request);
      case "checkout":
        return checkout(request);
      case "save":
        return save(request);
      default:
        throw new UnsupportedOperationException(request.getType());
    }
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
  public List<FileAttributes> listChildren(@Nonnull WorkspaceRequest request) throws IOException {
    List<FileAttributes> ret = new ArrayList<>();
    try(DirectoryStream<Path> stream = Files.newDirectoryStream(getGitPath(request))) {
      for(Path child : stream) {
        ret.add(FileAttributes.read(child));
      }
    }
    Collections.sort(ret);
    return ret;
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

}
