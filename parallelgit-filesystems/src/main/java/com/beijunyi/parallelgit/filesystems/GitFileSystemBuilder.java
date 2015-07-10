package com.beijunyi.parallelgit.filesystems;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
  private URI uri;
  private Map<String, ?> properties;
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
  public static GitFileSystemBuilder prepare(@Nonnull GitFileSystemProvider provider) {
    return new GitFileSystemBuilder().provider(provider);
  }

  @Nonnull
  public GitFileSystemBuilder provider(@Nullable GitFileSystemProvider provider) {
    this.provider = provider;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder uri(@Nullable URI uri) {
    this.uri = uri;
    return this;
  }

  @Nonnull
  public GitFileSystemBuilder properties(@Nullable Map<String, ?> properties) {
    this.properties = properties;
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

  private void errorDifferentBranches(@Nonnull String b1, @Nonnull String b2) {
    throw new IllegalArgumentException("Different branches found: " + b1 + ", " + b2);
  }

  private void errorDifferentRevisions(@Nonnull String r1, @Nonnull String r2) {
    throw new IllegalArgumentException("Different revisions found: " + r1 + ", " + r2);
  }

  private void errorDifferentTrees(@Nonnull String t1, @Nonnull String t2) {
    throw new IllegalArgumentException("Different trees found: " + t1 + ", " + t2);
  }

  private void readUri() {
    if(uri != null)
      applyRepoPathFromUri(uri);
    applyUriParams(GitUriUtils.getParams(uri, properties));
  }

  private void applyRepoPathFromUri(@Nonnull URI uri) {
    String repoDirPathFromUri = GitUriUtils.getRepoPath(uri);
    if(repoDirPath == null)
      repoDirPath = GitUriUtils.getRepoPath(uri);
    else if(!repoDirPath.equals(repoDirPathFromUri))
      errorDifferentRepositories(repoDirPath, repoDirPathFromUri);
  }

  private void applyUriParams(@Nonnull GitUriParams params) {
    applyBranchFromUri(params);
    applyRevisionFromUri(params);
    applyTreeFromUri(params);
  }

  private void applyBranchFromUri(@Nonnull GitUriParams params) {
    String branchFromUri = params.getBranch();
    if(branch == null)
      branch = branchFromUri;
    else if(branchFromUri != null && !branch.equals(branchFromUri))
      errorDifferentBranches(branch, branchFromUri);
  }

  private void applyRevisionFromUri(@Nonnull GitUriParams params) {
    String revisionFromUri = params.getRevision();
    if(commitIdStr == null)
      commitIdStr = revisionFromUri;
    else if(revisionFromUri != null && !commitIdStr.equals(revisionFromUri))
      errorDifferentRevisions(commitIdStr, revisionFromUri);
  }

  private void applyTreeFromUri(@Nonnull GitUriParams params) {
    String treeFromUri = params.getTree();
    if(treeIdStr == null)
      treeIdStr = treeFromUri;
    else if(treeFromUri != null && !treeIdStr.equals(treeFromUri))
      errorDifferentTrees(treeIdStr, treeFromUri);
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
    readUri();
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
