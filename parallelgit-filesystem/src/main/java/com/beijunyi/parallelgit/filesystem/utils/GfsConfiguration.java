package com.beijunyi.parallelgit.filesystem.utils;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.HeadAlreadyDefinedException;
import com.beijunyi.parallelgit.filesystem.exceptions.RepositoryAlreadyDefinedException;
import com.beijunyi.parallelgit.utils.BranchUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import static com.beijunyi.parallelgit.filesystem.GitFileSystemProvider.GFS;
import static com.beijunyi.parallelgit.utils.RepositoryUtils.*;
import static org.eclipse.jgit.lib.Constants.MASTER;

public class GfsConfiguration {

  private Repository repo;
  private File repoDir = new File(GFS);
  private Boolean create = null;
  private Boolean bare = null;
  private String branch = MASTER;
  private RevCommit commit;

  public GfsConfiguration(@Nonnull Repository repo) {
    this.repo = repo;
  }

  public GfsConfiguration(@Nonnull File repoDir) throws IOException {
    this(openRepository(repoDir));
  }

  public GfsConfiguration(@Nonnull String repoDir) throws IOException {
    this(new File(repoDir));
  }

  @Nonnull
  public Repository repository() {
    return repo;
  }

  @Nonnull
  public GfsConfiguration repository(@Nonnull Repository repo) {
    this.repo = repo;
    repoDir = repo.getDirectory();
    return this;
  }

  @Nonnull
  public GfsConfiguration repository(@Nonnull File repoDir) {
    if(this.repoDir != null && !this.repoDir.equals(repoDir))
      throw new RepositoryAlreadyDefinedException();
    this.repoDir = repoDir;
    return this;
  }

  @Nonnull
  public GfsConfiguration create(boolean create) {
    this.create = create;
    return this;
  }

  @Nonnull
  public GfsConfiguration create() {
    return create(true);
  }

  @Nonnull
  public GfsConfiguration bare(boolean bare) {
    this.bare = bare;
    return this;
  }

  @Nonnull
  public GfsConfiguration bare() {
    return bare(true);
  }

  @Nonnull
  public GfsConfiguration branch(@Nonnull String branch) throws IOException {
    if(commit != null)
      throw new HeadAlreadyDefinedException();
    this.branch = branch;
    return this;
  }

  @Nonnull
  public GfsConfiguration commit(@Nonnull RevCommit commit) {
    if(branch != null)
      throw new HeadAlreadyDefinedException();
    this.commit = commit;
    return this;
  }

  @Nonnull
  public GfsConfiguration commit(@Nonnull AnyObjectId commit) throws IOException {
    return commit(CommitUtils.getCommit(commit, repo));
  }

  @Nonnull
  public GfsConfiguration commit(@Nonnull String commit) throws IOException {
    return commit(CommitUtils.getCommit(commit, repo));
  }

  @Nonnull
  public GitFileSystem build() throws IOException {
    if(branch == null && commit == null)
      branch = MASTER;
    if(commit == null) {
      AnyObjectId commitId = repo.resolve(branch);
      commit = commitId != null ? CommitUtils.getCommit(commitId, repo) : null;
    }
    if(branch != null && !BranchUtils.branchExists(branch, repo) && commit != null)
      branch = null;
    return new GitFileSystem(repo, branch, commit);
  }

  @Nonnull
  public GfsConfiguration readParams(@Nonnull GfsParams params) throws IOException {
    String branchValue = params.branch();
    if(branchValue != null)
      branch(branchValue);

    String commitValue = params.commit();
    if(commitValue != null)
      commit(commitValue);
    return this;
  }

  private void prepareRepository() throws IOException {
    if(repo == null) {
      if(create == null)
        create = !repoDir.exists();
      if(create && bare == null)
        bare = true;
      if(create)
        repo = createRepository(repoDir, bare);
      else
        repo = bare != null ? openRepository(repoDir, bare) : openRepository(repoDir);
    }
  }

}
