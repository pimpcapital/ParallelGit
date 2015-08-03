package com.beijunyi.parallelgit.filesystem.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.GitFileSystemProvider;
import com.beijunyi.parallelgit.utils.CommitHelper;
import com.beijunyi.parallelgit.utils.RefHelper;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitFileSystemBuilder {

  private GitFileSystemProvider provider;
  private Repository repository;
  private File repoDir;
  private String repoDirPath;
  private Boolean create;
  private Boolean bare;
  private String branch;
  private String branchRef;
  private RevCommit commit;
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
    return prepare()
             .repository(GitUriUtils.getRepository(uri))
             .readAllParams(GitParams.getParams(properties));
  }

  @Nonnull
  public static GitFileSystemBuilder forPath(@Nonnull Path path, @Nonnull Map<String, ?> properties) {
    return prepare()
             .repository(path.toFile())
             .readAllParams(GitParams.getParams(properties));
  }

  @Nonnull
  public GitFileSystemBuilder readAllParams(@Nonnull GitParams params) {
    return this
             .create(params.getCreate())
             .bare(params.getBare())
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
  public GitFileSystemBuilder create(@Nullable Boolean create) {
    this.create = create;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder create() {
    return create(true);
  }

  @Nonnull
  public GitFileSystemBuilder bare(@Nullable Boolean bare) {
    this.bare = bare;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder bare() {
    return bare(true);
  }

  @Nonnull
  public GitFileSystemBuilder branch(@Nullable String branch) {
    this.branch = branch;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder commit(@Nullable RevCommit commit) {
    this.commit = commit;
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

  private void errorUnsupportedLocation(@Nonnull String dir) {
    throw new UnsupportedOperationException(dir + " is not a valid location");
  }

  private void errorUnsupportedRepository(@Nonnull String dir) {
    throw new UnsupportedOperationException(dir + " is not a valid repository");
  }

  private void prepareProvider() {
    if(provider == null)
      provider = GitFileSystemProvider.getInstance();
  }

  private void prepareRepository() throws IOException {
    if(repository == null) {
      prepareRepoDir();
      repository = new FileRepository(repoDir);
      if(create != null && create)
        repository.create(bare == null || bare);
    }
    if(!repository.getObjectDatabase().exists())
      errorUnsupportedRepository(repository.getDirectory().getAbsolutePath());
  }

  private void prepareRepoDir() {
    if(repoDir == null) {
      if(repoDirPath == null)
        errorNoRepositories();
      repoDir = new File(repoDirPath);
    }
    if(!repoDir.isDirectory())
      errorUnsupportedLocation(repoDir.getAbsolutePath());
    useDotGit();
  }

  private void useDotGit() {
    File dotGit = new File(repoDir, Constants.DOT_GIT);
    if(bare == null) {
      if(dotGit.exists())
        repoDir = dotGit;
    } else if(!bare)
      repoDir = dotGit;
  }

  private void prepareBranch() throws IOException {
    if(branch != null) {
      branchRef = RefHelper.getBranchRefName(branch);
      branch = branchRef.substring(Constants.R_HEADS.length());
    }
  }

  private void prepareCommitId() throws IOException {
    if(commitId == null) {
      if(commitIdStr != null)
        commitId = repository.resolve(commitIdStr);
      else if(branch != null)
        commitId = repository.resolve(branchRef);
    }
  }

  private void prepareCommit() throws IOException {
    if(commit == null) {
      prepareCommitId();
      if(commitId != null)
        commit = CommitHelper.getCommit(repository, commitId);
    }

  }

  private void prepareTree() throws IOException {
    if(treeId == null) {
      if(treeIdStr != null)
        treeId = repository.resolve(treeIdStr);
      else if(commit != null)
        treeId = commit.getTree();
    }
  }

  @Nonnull
  public GitFileSystem build() throws IOException {
    prepareProvider();
    prepareRepository();
    prepareBranch();
    prepareCommit();
    prepareTree();
    GitFileSystem fs = new GitFileSystem(provider, repository, branch, commit, treeId);
    provider.register(fs);
    return fs;
  }
}
