package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exception.RefUpdateValidator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;

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
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nullable String message, @Nullable PersonIdent tagger, @Nonnull Repository repo, boolean force) throws IOException {
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
    return updateTagRef(tag, name, repo, force);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nullable String message, @Nullable PersonIdent tagger, @Nonnull Repository repo) throws IOException {
    return tagCommit(commitId, name, message, tagger, repo, false);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nullable String message, @Nonnull String taggerName, @Nonnull String taggerEmail, @Nonnull Repository repo, boolean force) throws IOException {
    PersonIdent tagger = new PersonIdent(taggerName, taggerEmail);
    return tagCommit(commitId, name, message, tagger, repo, force);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nullable String message, @Nonnull String taggerName, @Nonnull String taggerEmail, @Nonnull Repository repo) throws IOException {
    return tagCommit(commitId, name, message, taggerName, taggerEmail, repo, false);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nullable String message, @Nonnull Repository repo, boolean force) throws IOException {
    PersonIdent tagger = message != null ? new PersonIdent(repo) : null;
    return tagCommit(commitId, name, message, tagger, repo, force);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nullable String message, @Nonnull Repository repo) throws IOException {
    return tagCommit(commitId, name, message, repo, false);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nonnull Repository repo, boolean force) throws IOException {
    return tagCommit(commitId, name, null, repo, force);
  }

  @Nonnull
  public static Ref tagCommit(@Nonnull AnyObjectId commitId, @Nonnull String name, @Nonnull Repository repo) throws IOException {
    return tagCommit(commitId, name, repo, false);
  }

  @Nonnull
  public static Ref tagHeadCommit(@Nonnull String name, @Nullable String message, @Nonnull Repository repo, boolean force) throws IOException {
    if(repo.isBare())
      throw new IllegalArgumentException("Bare repository does not have head commit");
    AnyObjectId headCommit = repo.resolve(Constants.HEAD);
    return tagCommit(headCommit, name, message, repo, force);
  }

  @Nonnull
  public static Ref tagHeadCommit(@Nonnull String name, @Nullable String message, @Nonnull Repository repo) throws IOException {
    return tagHeadCommit(name, message, repo, false);
  }

  @Nonnull
  public static Ref tagHeadCommit(@Nonnull String name, @Nonnull Repository repo, boolean force) throws IOException {
    return tagHeadCommit(name, null, repo, force);
  }

  @Nonnull
  public static Ref tagHeadCommit(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    return tagHeadCommit(name, repo, false);
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
      return getTag(tagRef.getObjectId(), reader);
    }
  }

  @Nullable
  public static RevTag getTag(@Nonnull String tagName, @Nonnull Repository repo) throws IOException {
    Ref tagRef = repo.getRef(RefUtils.ensureTagRefName(tagName));
    return tagRef != null ? getTag(tagRef, repo) : null;
  }

}
