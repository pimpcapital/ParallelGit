package com.beijunyi.parallelgit.utils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.exception.RefUpdateValidator;
import org.eclipse.jgit.internal.storage.dfs.DfsGarbageCollector;
import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.internal.storage.file.GC;
import org.eclipse.jgit.lib.*;

public final class RepositoryUtils {

  @Nonnull
  public static Repository createRepository(@Nonnull File dir, boolean bare) throws IOException {
    Repository repo = new RepositoryBuilder()
                        .readEnvironment()
                        .setGitDir(bare ? dir : new File(dir, Constants.DOT_GIT))
                        .build();
    repo.create(bare);
    return repo;
  }

  @Nonnull
  public static Repository createRepository(@Nonnull File dir) throws IOException {
    return createRepository(dir, true);
  }

  @Nonnull
  public static Repository openRepository(@Nonnull File dir, boolean bare) throws IOException {
    if(!bare)
      return openRepository(new File(dir, Constants.DOT_GIT), true);
    return new FileRepository(dir);
  }

  @Nonnull
  public static Repository openRepository(@Nonnull File dir) throws IOException {
    return openRepository(dir, !new File(dir, Constants.DOT_GIT).exists());
  }

  @Nonnull
  private static RefUpdate prepareUpdateHead(@Nonnull Repository repo, @Nonnull String name, boolean detach) throws IOException {
    RefUpdate ret = repo.updateRef(Constants.HEAD, detach);
    ret.setForceUpdate(true);
    ret.setRefLogMessage("checkout: moving from HEAD to " + Repository.shortenRefName(name), false);
    return ret;
  }

  public static void attachRepositoryHead(@Nonnull Repository repo, @Nonnull String refName) throws IOException {
    RefUpdate update = prepareUpdateHead(repo, Repository.shortenRefName(refName), false);
    RefUpdateValidator.validate(update.link(refName));
  }

  public static void attachRepositoryHead(@Nonnull Repository repo, @Nonnull Ref ref) throws IOException {
    attachRepositoryHead(repo, ref.getName());
  }

  public static void detachRepositoryHead(@Nonnull Repository repo, @Nonnull AnyObjectId commitId) throws IOException {
    RefUpdate update = prepareUpdateHead(repo, commitId.getName(), true);
    update.setNewObjectId(commitId);
    RefUpdateValidator.validate(update.forceUpdate());
  }

  public static void setRepositoryHead(@Nonnull Repository repo, @Nonnull String name) throws IOException {
    Ref ref = repo.getRef(name);
    if(ref != null) {
      if(!ref.getName().startsWith(Constants.R_HEADS))
        detachRepositoryHead(repo, repo.resolve(name));
      else
        attachRepositoryHead(repo, ref);
    } else
      attachRepositoryHead(repo, RefUtils.ensureBranchRefName(name));
  }

  public static void garbageCollect(@Nonnull FileRepository repo) throws IOException {
    try {
      new GC(repo).gc();
    } catch(ParseException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void garbageCollect(@Nonnull DfsRepository repo) throws IOException {
    new DfsGarbageCollector(repo).pack(NullProgressMonitor.INSTANCE);
  }

  public static void garbageCollect(@Nonnull Repository repo) throws IOException {
    if(repo instanceof FileRepository)
      garbageCollect((FileRepository) repo);
    else if(repo instanceof DfsRepository)
      garbageCollect((DfsRepository) repo);
    else
      throw new UnsupportedOperationException("Unsupported repository: " + repo.getClass().getName());
  }



}
