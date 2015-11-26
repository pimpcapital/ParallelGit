package com.beijunyi.parallelgit.filesystem.utils;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.HeadAlreadyDefinedException;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class GfsBuilder {

  private final Repository repo;
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
  public GfsBuilder branch(@Nonnull String branch) throws IOException {
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
  public GitFileSystem build() throws IOException {
    if(branch == null && commit == null)
      branch = Constants.MASTER;
    if(commit == null) {
      AnyObjectId commitId = repo.resolve(branch);
      commit = commitId != null ? CommitUtils.getCommit(commitId, repo) : null;
    }
    if(branch != null && !BranchUtils.branchExists(branch, repo) && commit != null)
      branch = null;
    return new GitFileSystem(repo, branch, commit);
  }

  @Nonnull
  public GfsBuilder readParams(@Nonnull GfsParams params) throws IOException {
    String branchValue = params.getBranch();
    if(branchValue != null)
      branch(branchValue);

    String commitValue = params.getCommit();
    if(commitValue != null)
      commit(commitValue);
    return this;
  }

}
