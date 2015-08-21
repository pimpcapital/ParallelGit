package com.beijunyi.parallelgit.utils;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;

public final class RepositoryHelper {

  /**
   * Creates a new {@code Repository} in the given directory.
   *
   * @param   repoDir
   *          the directory of the git repository to create
   * @param   bare
   *          whether to create a {@code BARE} repository
   * @return  the result {@code Repository}
   */
  @Nonnull
  public static Repository createRepository(@Nonnull File repoDir, boolean bare) throws IOException {
    Repository repo = new RepositoryBuilder()
                        .readEnvironment()
                        .setGitDir(bare ? repoDir : new File(repoDir, Constants.DOT_GIT))
                        .build();
    repo.create(bare);
    return repo;
  }

  /**
   * Opens a {@code Repository} from the given directory.
   *
   * @param   repoDir
   *          the directory of the git repository to open
   * @return  the result {@code Repository}
   */
  @Nonnull
  public static Repository openRepository(@Nonnull File repoDir) throws IOException {
    File dotGit = new File(repoDir, Constants.DOT_GIT);
    if(dotGit.exists())
      return new FileRepository(dotGit);
    else
      return new FileRepository(repoDir);
  }


  /**
   * Sets the current HEAD of the repository to the given revision.
   * This method behaves similarly to {@code CheckoutCommand}. The main difference is this method will not change the contents in the working directory.
   * Note that this method will do nothing if the given repository is bare.
   *
   * @param repo a git repository
   * @param revision a revision reference
   */
  @Nonnull
  public static RefUpdate.Result setRepositoryHead(@Nonnull Repository repo, @Nonnull String revision) throws IOException {
    if(repo.isBare())
      throw new IllegalArgumentException(repo + " is a bare repository.");
    Ref ref = repo.getRef(revision);
    if(ref != null && !ref.getName().startsWith(Constants.R_HEADS))
      ref = null;

    Ref headRef = repo.getRef(Constants.HEAD);
    String shortHeadRef = Repository.shortenRefName(headRef.getName());
    String refLogMessage = "checkout: moving from " + shortHeadRef;

    RefUpdate refUpdate = repo.updateRef(Constants.HEAD, ref == null);
    refUpdate.setForceUpdate(true);
    refUpdate.setRefLogMessage(refLogMessage + " to " + Repository.shortenRefName(revision), false);

    if(ref != null)
      return refUpdate.link(ref.getName());
    else {
      refUpdate.setNewObjectId(repo.resolve(revision));
      return refUpdate.forceUpdate();
    }
  }

}
