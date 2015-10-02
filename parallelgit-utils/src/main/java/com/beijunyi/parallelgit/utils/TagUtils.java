package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exception.NoSuchTagException;
import com.beijunyi.parallelgit.utils.exception.RefUpdateValidator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;

public final class TagUtils {

  public static boolean tagExists(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    Ref tagRef = repo.getRef(RefUtils.ensureTagRefName(name));
    return tagRef != null;
  }

  @Nonnull
  public static AnyObjectId getTaggedCommit(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    Ref tagRef = RefUtils.getTagRef(name, repo);
    if(tagRef == null)
      throw new NoSuchTagException(name);
    AnyObjectId ret = tagRef.getPeeledObjectId();
    return ret != null ? ret : CommitUtils.getCommit(tagRef.getObjectId(), repo);
  }

  @Nonnull
  public static Ref updateTagRef(@Nonnull AnyObjectId tagId, @Nonnull String name, @Nonnull Repository repo) throws IOException {
    String refName = RefUtils.ensureTagRefName(name);
    RefUpdate update = repo.updateRef(refName);
    update.setNewObjectId(tagId);
    update.setRefLogMessage("tagged " + name, false);
    RefUpdateValidator.validate(update.update());
    return repo.getRef(refName);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nullable String message, @Nullable PersonIdent tagger, @Nonnull Repository repo) throws IOException {
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
    return updateTagRef(tag, name, repo);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nullable String message, @Nonnull Repository repo) throws IOException {
    return tagCommit(commitId, name, message, new PersonIdent(repo), repo);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nonnull Repository repo) throws IOException {
    return tagCommit(commitId, name, null, repo);
  }

  @Nonnull
  public static Ref tagHeadCommit(@Nonnull String name, @Nullable String message, @Nonnull Repository repo) throws IOException {
    if(repo.isBare())
      throw new IllegalArgumentException("Bare repository does not have head commit");
    AnyObjectId headCommit = repo.resolve(Constants.HEAD);
    return tagCommit(headCommit, name, message, repo);
  }

  @Nonnull
  public static Ref tagHeadCommit(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    return tagHeadCommit(name, null, repo);
  }

  @Nonnull
  public static RevTag getTag(@Nonnull AnyObjectId tagObjectId, @Nonnull ObjectReader reader) throws IOException {
    try(RevWalk revWalk = new RevWalk(reader)) {
      return revWalk.parseTag(tagObjectId);
    }
  }

  @Nonnull
  public static RevTag getTag(@Nonnull Ref tagRef, @Nonnull ObjectReader reader) throws IOException {
    return getTag(tagRef.getObjectId(), reader);
  }

  @Nonnull
  public static RevTag getTag(@Nonnull Ref tagRef, @Nonnull Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getTag(tagRef, reader);
    }
  }

  @Nullable
  public static RevTag getTag(@Nonnull String tagName, @Nonnull Repository repo) throws IOException {
    Ref tagRef = repo.getRef(RefUtils.ensureTagRefName(tagName));
    return tagRef != null ? getTag(tagRef, repo) : null;
  }

}
