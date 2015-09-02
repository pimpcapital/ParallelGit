package com.beijunyi.parallelgit.utils;

import java.io.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.exception.RefUpdateValidator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.transport.PackParser;

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
  public static Repository openRepository(@Nonnull File dir) throws IOException {
    File dotGit = new File(dir, Constants.DOT_GIT);
    if(dotGit.exists())
      return new FileRepository(dotGit);
    else
      return new FileRepository(dir);
  }


  public static void setRepositoryHead(@Nonnull Repository repo, @Nonnull String revision) throws IOException {
    if(repo.isBare())
      return;
    Ref ref = repo.getRef(revision);
    if(ref != null && !ref.getName().startsWith(Constants.R_HEADS))
      ref = null;

    Ref headRef = repo.getRef(Constants.HEAD);
    String shortHeadRef = Repository.shortenRefName(headRef.getName());
    String refLogMessage = "checkout: moving from " + shortHeadRef;

    RefUpdate refUpdate = repo.updateRef(Constants.HEAD, ref == null);
    refUpdate.setForceUpdate(true);
    refUpdate.setRefLogMessage(refLogMessage + " to " + Repository.shortenRefName(revision), false);

    RefUpdate.Result result;
    if(ref != null)
      result =  refUpdate.link(ref.getName());
    else {
      refUpdate.setNewObjectId(repo.resolve(revision));
      result = refUpdate.forceUpdate();
    }
    RefUpdateValidator.validate(shortHeadRef, result);
  }



}
