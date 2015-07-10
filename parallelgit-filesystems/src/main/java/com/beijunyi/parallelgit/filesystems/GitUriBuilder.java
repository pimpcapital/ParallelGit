package com.beijunyi.parallelgit.filesystems;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitUriBuilder {

  private String session;
  private String repository;
  private String file;
  private String branch;
  private String revision;
  private String tree;

  @Nonnull
  public static GitUriBuilder prepare() {
    return new GitUriBuilder();
  }

  @Nonnull
  public static GitUriBuilder forGitFileSystem(@Nonnull GitFileSystem gfs) throws IOException {
    GitUriBuilder builder = prepare().session(gfs.getSessionId());
    GitFileStore store = gfs.getFileStore();
    AnyObjectId tree = store.getBaseTree();
    RevCommit commit = store.getBaseCommit();
    String branch = store.getBranch();
    Repository repository = store.getRepository();
    if(tree != null && (commit == null || !commit.getTree().equals(tree)))
      builder.tree(tree);
    if(commit != null && (branch == null || !repository.resolve(branch).equals(commit)))
      builder.revision(commit);
    if(branch != null)
      builder.branch(branch);
    return builder;
  }

  @Nonnull
  public GitUriBuilder session(@Nullable String session) {
    this.session = session;
    return this;
  }

  @Nonnull
  public GitUriBuilder repository(@Nullable String repoDirPath) {
    this.repository = repoDirPath;
    return this;
  }

  @Nonnull
  public GitUriBuilder repository(@Nullable File repoDir) {
    return repository(repoDir != null ? repoDir.toURI().getPath() : null);
  }

  @Nonnull
  public GitUriBuilder repository(@Nullable Repository repository) {
    return repository(repository != null
                        ? (repository.isBare() ? repository.getDirectory() : repository.getWorkTree())
                        : null);
  }

  @Nonnull
  public GitUriBuilder file(@Nullable String filePathStr) {
    this.file = filePathStr;
    return this;
  }

  @Nonnull
  public GitUriBuilder file(@Nullable GitPath filePath) {
    return file(filePath != null ? filePath.getNormalizedString() : null);
  }

  @Nonnull
  public GitUriBuilder branch(@Nullable String branch) {
    this.branch = branch;
    return this;
  }

  @Nonnull
  public GitUriBuilder revision(@Nullable String revisionIdStr) {
    this.revision = revisionIdStr;
    return this;
  }

  @Nonnull
  public GitUriBuilder revision(@Nullable AnyObjectId revisionId) {
    return revision(revisionId != null ? revisionId.getName() : null);
  }

  @Nonnull
  public GitUriBuilder tree(@Nullable String treeIdStr) {
    this.tree = treeIdStr;
    return this;
  }

  @Nonnull
  public GitUriBuilder tree(@Nullable AnyObjectId treeId) {
    return tree(treeId != null ? treeId.getName() : null);
  }

  @Nonnull
  private String buildPath() {
    String path = GitFileSystemProvider.GIT_FS_SCHEME + "://" + repository;
    if(file != null && !file.isEmpty() && !file.equals("/"))
      path += GitFileSystemProvider.ROOT_SEPARATOR + file;
    return path;
  }

  @Nonnull
  private String buildQuery() {
    String query = "";
    if(session != null)
      query += "&" + GitFileSystemProvider.SESSION_KEY + "=" + session;
    if(branch != null)
      query += "&" + GitFileSystemProvider.BRANCH_KEY + "=" + branch;
    if(revision != null)
      query += "&" + GitFileSystemProvider.REVISION_KEY + "=" + revision;
    if(tree != null)
      query += "&" + GitFileSystemProvider.TREE_KEY + "=" + tree;
    return !query.isEmpty() ? query.substring(1) : query;
  }

  @Nonnull
  public URI build() {
    String path = buildPath();
    String query = buildQuery();
    if(!query.isEmpty())
      path += "?" + query;
    return URI.create(path);
  }
}
