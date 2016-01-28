package com.beijunyi.parallelgit.web.data;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.Repository;

@Named
@Singleton
public class RepositoryManager {

  private final Repository repo;

  @Inject
  public RepositoryManager() throws IOException {
    Repository fileRepo = getFileRepository();
    repo = fileRepo != null ? fileRepo : new InMemoryRepository(new DfsRepositoryDescription());
  }

  @Nonnull
  public Repository getRepository() {
    return repo;
  }

  @Nullable
  private static Repository getFileRepository() throws IOException {
    String path = getSetting("repository");
    if(path == null)
      return null;
    File repoDir = new File(path);
    return repoDir.exists() ? RepositoryUtils.openRepository(repoDir) : RepositoryUtils.createRepository(repoDir);
  }

  @Nullable
  private static String getSetting(@Nonnull String key) {
    String ret = System.getProperty(key);
    if(ret == null)
      ret = System.getenv(key);
    return ret;
  }


}
