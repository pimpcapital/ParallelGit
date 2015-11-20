package com.beijunyi.parallelgit.filesystem.utils;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.HeadAlreadyDefinedException;
import com.beijunyi.parallelgit.filesystem.exceptions.NoRepositoryException;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.utils.RefUtils.ensureBranchRefName;

public class GfsBuilder {

  private final Repository repo;

  private File repoDir;
  private Boolean create;
  private Boolean bare;
  private String branch;
  private RevCommit commit;

  public GfsBuilder(@Nonnull Repository repo) {
    this.repo = repo;
  }

  public GfsBuilder(@Nonnull File repoDir) throws IOException {
    this(RepositoryUtils.openRepository(repoDir));
  }

  public GfsBuilder(@Nonnull String repoDir) throws IOException {
    this(new File(repoDir));
  }

  @Nonnull
  public GfsBuilder create(@Nullable Boolean create) {
    this.create = create;
    return this;
  }

  @Nonnull
  public GfsBuilder create() {
    return create(true);
  }

  @Nonnull
  public GfsBuilder bare(@Nullable Boolean bare) {
    this.bare = bare;
    return this;
  }

  @Nonnull
  public GfsBuilder bare() {
    return bare(true);
  }

  @Nonnull
  public GfsBuilder branch(@Nonnull String branch) {
    if(commit != null)
      throw new HeadAlreadyDefinedException();
    this.branch = branch;
    return this;
  }

  @Nonnull
  public GfsBuilder commit(@Nonnull RevCommit commit) {
    if(branch != null)
      throw new HeadAlreadyDefinedException();
    this.commit = commit;
    return this;
  }

  @Nonnull
  public GfsBuilder commit(@Nonnull AnyObjectId commit) throws IOException {
    return commit(CommitUtils.getCommit(commit, repo));
  }

  @Nonnull
  public GfsBuilder commit(@Nonnull String commit) throws IOException {
    return commit(CommitUtils.getCommit(commit, repo));
  }

  @Nonnull
  public GfsBuilder revision(@Nonnull String revision) throws IOException {
    if(BranchUtils.branchExists(revision, repo))
      return branch(revision);
    return commit(revision);
  }

  @Nonnull
  public GitFileSystem build() throws IOException {
    prepareRepository();
    prepareBranch();
    prepareCommit();
    return new GitFileSystem(repo, commit, branch);
  }

  @Nonnull
  public GfsBuilder readParams(@Nonnull GfsParams params) throws IOException {
    return this
             .create(params.getCreate())
             .bare(params.getBare())
             .branch(params.getBranch())
             .commit(params.getCommit());
  }

  private void prepareRepository() throws IOException {
    if(repository == null) {
      prepareRepoDir();
      repository = new FileRepository(repoDir);
      if(create != null && create)
        repository.create(bare == null || bare);
    }
  }

  private void prepareRepoDir() {
    if(repoDir == null) {
      if(repoDirPath == null)
        throw new NoRepositoryException();
      repoDir = new File(repoDirPath);
    }
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
    if(branch == null && revision != null && BranchUtils.branchExists(revision, repository))
      branch = revision;
    if(branch != null) {
      branchRef = ensureBranchRefName(branch);
      branch = branchRef.substring(Constants.R_HEADS.length());
    }
  }

  private void prepareCommitId() throws IOException {
    if(commitId == null) {
      if(commitIdStr != null)
        commitId = repository.resolve(commitIdStr);
      else if(branch == null && revision != null)
        commitId = repository.resolve(revision);
      else if(branch != null)
        commitId = repository.resolve(branchRef);
    }
  }

  private void prepareCommit() throws IOException {
    if(commit == null) {
      prepareCommitId();
      if(commitId != null)
        commit = CommitUtils.getCommit(commitId, repository);
    }
  }


}
