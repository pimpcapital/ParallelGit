package com.beijunyi.parallelgit.filesystems;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.RefHelper;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

public class GitFileSystemBuilder {

  private GitFileSystemProvider provider;
  private Repository repository;
  private File repoDir;
  private String repoDirPath;
  private String branch;
  private AnyObjectId commitId;
  private String commitIdStr;
  private AnyObjectId treeId;
  private String treeIdStr;

  @Nonnull
  public static GitFileSystemBuilder prepare() {
    return new GitFileSystemBuilder();
  }

  @Nonnull
  public static GitFileSystemBuilder forUri(@Nonnull URI uri, @Nonnull Map<String, ?> properties) {
    GitUriParams params = GitUriParams.getParams(uri);
    params.extend(GitUriParams.getParams(properties));
    return prepare()
             .repository(GitUriUtils.getRepository(uri))
             .branch(params.getBranch())
             .commit(params.getRevision())
             .tree(params.getTree());
  }

  @Nonnull
  public static GitFileSystemBuilder forPath(@Nonnull Path path, @Nonnull Map<String, ?> properties) {
    GitUriParams params = GitUriParams.getParams(properties);
    return prepare()
             .repository(path.toFile())
             .branch(params.getBranch())
             .commit(params.getRevision())
             .tree(params.getTree());
  }

  @Nonnull
  public GitFileSystemBuilder provider(@Nullable GitFileSystemProvider provider) {
    this.provider = provider;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder repository(@Nullable Repository repository) {
    this.repository = repository;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder repository(@Nullable File repoDir) {
    this.repoDir = repoDir;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder repository(@Nullable String repoDirPath) {
    this.repoDirPath = repoDirPath;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder branch(@Nullable String branch) {
    this.branch = branch;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder commit(@Nullable AnyObjectId commitId) {
    this.commitId = commitId;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder commit(@Nullable String commitIdStr) {
    this.commitIdStr = commitIdStr;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder tree(@Nullable AnyObjectId treeId) {
    this.treeId = treeId;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder tree(@Nullable String treeIdStr) {
    this.treeIdStr = treeIdStr;
    return this;
  }

  private void errorNoRepositories() {
    throw new IllegalArgumentException("No repository provided");
  }

  private void errorDifferentRepositories(@Nonnull String r1, @Nonnull String r2) {
    throw new IllegalArgumentException("Different repositories found: " + r1 + ", " + r2);
  }

  private void errorDifferentRevisions(@Nonnull String r1, @Nonnull String r2) {
    throw new IllegalArgumentException("Different revisions found: " + r1 + ", " + r2);
  }

  private void errorDifferentTrees(@Nonnull String t1, @Nonnull String t2) {
    throw new IllegalArgumentException("Different trees found: " + t1 + ", " + t2);
  }

  private void prepareProvider() {
    if(provider == null)
      provider = GitFileSystemProvider.getInstance();
  }

  private void prepareRepository() throws IOException {
    if(repository == null) {
      prepareRepoDir();
      repository = new FileRepository(repoDir);
    } else {
      if(repoDir != null && !repository.getDirectory().equals(repoDir))
        errorDifferentRepositories(repository.getDirectory().toString(), repoDir.toString());
      if(repoDirPath != null && !repository.getDirectory().equals(new File(repoDirPath)))
        errorDifferentRepositories(repository.getDirectory().toString(), repoDirPath);
    }
  }

  private void prepareRepoDir() {
    if(repoDir == null) {
      if(repoDirPath == null)
        errorNoRepositories();
      repoDir = new File(repoDirPath);
    } else if(repoDirPath != null && !repoDir.equals(new File(repoDirPath)))
      errorDifferentRepositories(repoDir.toString(), repoDirPath);
    useDotGit();
  }

  private void useDotGit() {
    File dotGit = new File(repoDir, Constants.DOT_GIT);
    if(dotGit.exists())
      repoDir = dotGit;
  }

  private void prepareBranch() throws IOException {
    if(branch != null)
      branch = RefHelper.getBranchRefName(branch);
  }

  private void prepareRevision() throws IOException {
    if(commitId == null) {
      if(commitIdStr != null)
        commitId = repository.resolve(commitIdStr);
      else if(branch != null)
        commitId = repository.resolve(branch);
    } else if(commitIdStr != null && !commitId.getName().equals(commitIdStr))
      errorDifferentRevisions(commitId.getName(), commitIdStr);
  }

  private void prepareTree() throws IOException {
    if(treeId == null) {
      if(treeIdStr != null)
        treeId = repository.resolve(treeIdStr);
    } else if(treeIdStr != null && !treeId.getName().equals(treeIdStr))
      errorDifferentTrees(treeId.getName(), treeIdStr);
  }

  @Nonnull
  public GitFileSystem build() throws IOException {
    prepareProvider();
    prepareRepository();
    prepareBranch();
    prepareRevision();
    prepareTree();
    GitFileSystem fs = new GitFileSystem(provider, repository, branch, commitId, treeId);
    provider.register(fs);
    return fs;
  }
}
