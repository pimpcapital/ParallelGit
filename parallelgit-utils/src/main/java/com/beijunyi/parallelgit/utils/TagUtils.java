package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.exception.RefUpdateValidator;
import org.eclipse.jgit.lib.*;

public final class TagUtils {

  @Nonnull
  public static Ref updateTagRef(@Nonnull AnyObjectId tagId, @Nonnull String name, @Nonnull Repository repo, boolean force) throws IOException {
    String refName = RefUtils.ensureTagRefName(name);
    RefUpdate update = repo.updateRef(refName);
    update.setNewObjectId(tagId);
    update.setForceUpdate(force);
    update.setRefLogMessage("tagged " + name, false);
    RefUpdateValidator.validate(update.update());
    return repo.getRef(refName);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nonnull String message, @Nonnull Repository repo, @Nonnull PersonIdent tagger) throws IOException {
    TagBuilder builder = new TagBuilder();
    builder.setTag(name);
    builder.setMessage(message);
    builder.setTagger(tagger);
    builder.setObjectId(commitId, Constants.OBJ_COMMIT);
    AnyObjectId tag;
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      tag = inserter.insert(builder);
      inserter.flush();
    }
    return updateTagRef(tag, name, repo, false);
  }

  public static void tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nonnull String message, @Nonnull String taggerName, @Nonnull Repository repo, @Nonnull String taggerEmail) throws IOException {
    PersonIdent tagger = new PersonIdent(taggerName, taggerEmail);
    tagCommit(commitId, name, message, repo, tagger);
  }

  public static void tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nonnull String message, @Nonnull Repository repo) throws IOException {
    PersonIdent tagger = new PersonIdent(repo);
    tagCommit(commitId, name, message, repo, tagger);
  }

  public static void tagHeadCommit(@Nonnull String name, @Nonnull String message, @Nonnull Repository repo) throws IOException {
    if(repo.isBare())
      throw new IllegalArgumentException("Bare repository does not have head commit");
    AnyObjectId headCommit = repo.resolve(Constants.HEAD);
    tagCommit(headCommit, name, message, repo);
  }


}
