package com.beijunyi.parallelgit.utils;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.ParallelGitException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;

public final class RepositoryHelper {

  /**
   * Creates a new git repository at the specified path or at ".git" under this path if the repository to be created is
   * bare.
   *
   * @param repoDir the directory that the new repository bases on
   * @param bare whether the repository to be created is bare
   * @return a new git repository
   */
  @Nonnull
  public static Repository newRepository(@Nonnull File repoDir, boolean bare) {
    try {
      RepositoryBuilder builder = new RepositoryBuilder();
      builder.readEnvironment();
      builder.setGitDir(bare ? repoDir : new File(repoDir, Constants.DOT_GIT));
      Repository repo = builder.build();
      repo.create(bare);
      return repo;
    } catch(IOException e) {
      throw new ParallelGitException("Could not create repository at " + repoDir, e);
    }
  }

  /**
   * Opens a file repository at the given path or at {@code .git} under the given path if the repository to be opened is bare.
   *
   * @param repoDir the directory of a git repository
   * @return the repository at the given path
   */
  @Nonnull
  public static Repository openRepository(@Nonnull File repoDir, boolean bare) {
    try {
      return new FileRepository(bare ? repoDir : new File(repoDir, Constants.DOT_GIT));
    } catch(IOException e) {
      throw new ParallelGitException("Could not open repository at " + repoDir, e);
    }
  }


  /**
   * Sets the current HEAD of the repository to the given revision.
   * This method behaves similarly to {@code CheckoutCommand}. The main difference is this method will not change the contents in the working directory.
   * Note that this method will do nothing if the given repository is bare.
   *
   * @param repo a git repository
   * @param revision a revision reference
   */
  public static void setRepositoryHead(@Nonnull Repository repo, @Nonnull String revision) {
    if(repo.isBare())
      return;
    try {
      Ref ref = repo.getRef(revision);
      if (ref != null && !ref.getName().startsWith(Constants.R_HEADS))
        ref = null;

      Ref headRef = repo.getRef(Constants.HEAD);
      String shortHeadRef = Repository.shortenRefName(headRef.getName());
      String refLogMessage = "checkout: moving from " + shortHeadRef;

      RefUpdate refUpdate = repo.updateRef(Constants.HEAD, ref == null);
      refUpdate.setForceUpdate(true);
      refUpdate.setRefLogMessage(refLogMessage + " to " + Repository.shortenRefName(revision), false);

      if(ref != null)
        refUpdate.link(ref.getName());
      else {
        refUpdate.setNewObjectId(repo.resolve(revision));
        refUpdate.forceUpdate();
      }

    } catch(IOException e) {
      throw new ParallelGitException("Could not update current branch to " + revision, e);
    }
  }

  @Nullable
  public static ObjectId getRevisionId(@Nonnull Repository repo, @Nonnull String revision) {
    try {
      return repo.resolve(revision);
    } catch(IOException e) {
      throw new ParallelGitException("Could not get the object id for revision " + revision, e);
    }
  }

  public static long getObjectSize(@Nonnull ObjectReader reader, @Nonnull AnyObjectId objectId, int typeHint) {
    try {
      return reader.getObjectSize(objectId, typeHint);
    } catch(IOException e) {
      throw new ParallelGitException("Could not get the size of object " + objectId.getName(), e);
    }
  }

  @Nonnull
  public static ObjectLoader open(@Nonnull ObjectReader reader, @Nonnull AnyObjectId objectId) {
    try {
      return reader.open(objectId);
    } catch(IOException e) {
      throw new ParallelGitException("Could not open " + objectId, e);
    }
  }

  public static void flush(@Nonnull ObjectInserter inserter) {
    try {
      inserter.flush();
    } catch(IOException e) {
      throw new ParallelGitException("Could not flush inserter", e);
    }
  }

}
