package com.beijunyi.parallelgit.utils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.exceptions.RefUpdateValidator;
import org.eclipse.jgit.internal.storage.dfs.DfsGarbageCollector;
import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.internal.storage.file.GC;
import org.eclipse.jgit.lib.*;

import static com.beijunyi.parallelgit.utils.RefUtils.fullBranchName;
import static org.eclipse.jgit.lib.ConfigConstants.*;
import static org.eclipse.jgit.lib.Constants.*;

public final class RepositoryUtils {

  @Nonnull
  public static Repository createRepository(File dir, boolean bare) throws IOException {
    Repository repo = new RepositoryBuilder()
                        .readEnvironment()
                        .setGitDir(bare ? dir : new File(dir, DOT_GIT))
                        .build();
    repo.create(bare);
    return repo;
  }

  @Nonnull
  public static Repository createRepository(File dir) throws IOException {
    return createRepository(dir, true);
  }

  @Nonnull
  public static Repository openRepository(File dir, boolean bare) throws IOException {
    if(!bare)
      return openRepository(new File(dir, DOT_GIT), true);
    return new FileRepository(dir);
  }

  @Nonnull
  public static Repository openRepository(File dir) throws IOException {
    return openRepository(dir, !new File(dir, DOT_GIT).exists());
  }

  public static void setDefaultCommitter(String name, String email, Repository repo) throws IOException {
    StoredConfig config = repo.getConfig();
    config.setString(CONFIG_USER_SECTION, null, CONFIG_KEY_NAME, name);
    config.setString(CONFIG_USER_SECTION, null, CONFIG_KEY_EMAIL, email);
    config.save();
  }

  public static boolean isRefLogEnabled(Repository repo) {
    StoredConfig config = repo.getConfig();
    return config.getBoolean(CONFIG_CORE_SECTION, null, CONFIG_KEY_LOGALLREFUPDATES, false);
  }

  public static void setRefLogEnabled(boolean enabled, Repository repo) {
    StoredConfig config = repo.getConfig();
    config.setBoolean(CONFIG_CORE_SECTION, null, CONFIG_KEY_LOGALLREFUPDATES, enabled);
  }

  @Nonnull
  private static RefUpdate prepareUpdateHead(Repository repo, String name, boolean detach) throws IOException {
    RefUpdate ret = repo.updateRef(Constants.HEAD, detach);
    ret.setForceUpdate(true);
    ret.setRefLogMessage("checkout: moving from HEAD to " + Repository.shortenRefName(name), false);
    return ret;
  }

  public static void attachRepositoryHead(Repository repo, String refName) throws IOException {
    RefUpdate update = prepareUpdateHead(repo, Repository.shortenRefName(refName), false);
    RefUpdateValidator.validate(update.link(refName));
  }

  public static void attachRepositoryHead(Repository repo, Ref ref) throws IOException {
    attachRepositoryHead(repo, ref.getName());
  }

  public static void detachRepositoryHead(Repository repo, AnyObjectId id) throws IOException {
    id = CommitUtils.getCommit(id, repo);
    RefUpdate update = prepareUpdateHead(repo, id.getName(), true);
    update.setNewObjectId(id);
    RefUpdateValidator.validate(update.forceUpdate());
  }

  public static void setRepositoryHead(Repository repo, String name) throws IOException {
    Ref ref = repo.findRef(name);
    if(ref != null) {
      if(!ref.getName().startsWith(R_HEADS))
        detachRepositoryHead(repo, repo.resolve(name));
      else
        attachRepositoryHead(repo, ref);
    } else
      attachRepositoryHead(repo, fullBranchName(name));
  }

  public static void garbageCollect(FileRepository repo) throws IOException {
    try {
      new GC(repo).gc();
    } catch(ParseException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void garbageCollect(DfsRepository repo) throws IOException {
    new DfsGarbageCollector(repo).pack(NullProgressMonitor.INSTANCE);
  }

  public static void garbageCollect(Repository repo) throws IOException {
    if(repo instanceof FileRepository)
      garbageCollect((FileRepository) repo);
    else if(repo instanceof DfsRepository)
      garbageCollect((DfsRepository) repo);
    else
      throw new UnsupportedOperationException("Unsupported repository: " + repo.getClass().getName());
  }



}
