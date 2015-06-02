package com.beijunyi.parallelgit.util.builder;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.util.RepositoryHelper;
import org.eclipse.jgit.lib.Repository;

public abstract class ParallelBuilder<T> {

  protected Repository repository;
  protected File repoDir;

  public void setRepository(@Nullable Repository repository) {
    this.repository = repository;
  }

  public void setRepoDir(@Nullable File repoDir) {
    this.repoDir = repoDir;
  }

  private void ensureRepository() throws IOException {
    if(repository == null && repoDir == null)
      throw new IllegalArgumentException("either of repository or repoDir must be configured");
    if(repository == null) {
      repository = RepositoryHelper.openRepository(repoDir);
    }
  }

  protected abstract T doBuild() throws IOException;

  @Nullable
  public T build() throws IOException {
    ensureRepository();
    return doBuild();
  }

}
