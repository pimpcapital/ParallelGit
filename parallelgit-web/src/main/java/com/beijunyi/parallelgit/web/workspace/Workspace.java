package com.beijunyi.parallelgit.web.workspace;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.io.GitFileAttributeView;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.web.data.FileEntry;
import com.beijunyi.parallelgit.web.data.Head;
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
  public Object processRequest(@Nonnull WorkspaceRequest request) throws IOException {
    switch(request.getType()) {
      case "head":
        return getHead();
      case "branches":
        return getBranches();
      case "directory":
        return getDirectory(request);
      case "file":
        return getFile(request);
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
  public List<FileEntry> getDirectory(@Nonnull WorkspaceRequest request) throws IOException {
    checkFS();
    String path = request.getTarget();
    if(path == null)
      throw new IllegalStateException();
    List<FileEntry> ret = new ArrayList<>();
    try(DirectoryStream<Path> children = Files.newDirectoryStream(gfs.getPath(path))) {
      for(Path child : children) {
        ret.add(FileEntry.read(child));
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
  public FileEntry save(@Nonnull WorkspaceRequest request) throws IOException {
    checkFS();
    String path = request.getTarget();
    String data = request.getValue();
    if(path == null || data == null)
      throw new IllegalStateException();
    Path file = gfs.getPath(path);
    Files.write(file, data.getBytes());
    return FileEntry.read(file);
  }

  @Override
  public void close() throws IOException {
    if(gfs != null)
      gfs.close();
  }

  private void checkFS() {
    if(gfs == null)
      throw new IllegalStateException();
  }

}
