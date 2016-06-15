package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exceptions.NoSuchTagException;
import com.beijunyi.parallelgit.utils.exceptions.RefUpdateValidator;
import com.beijunyi.parallelgit.utils.exceptions.TagAlreadyExistsException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;

import static org.eclipse.jgit.lib.Constants.OBJ_COMMIT;

public final class TagUtils {

  public static boolean tagExists(String name, Repository repo) throws IOException {
    Ref tagRef = repo.exactRef(RefUtils.fullTagRef(name));
    return tagRef != null;
  }

  @Nonnull
  public static ObjectId getTaggedCommit(String name, Repository repo) throws IOException {
    Ref tagRef = RefUtils.getTagRef(name, repo);
    if(tagRef == null)
      throw new NoSuchTagException(name);
    ObjectId ret = tagRef.getPeeledObjectId();
    return ret != null ? ret : CommitUtils.getCommit(tagRef.getObjectId(), repo);
  }

  @Nonnull
  public static Ref tagCommit(String name, AnyObjectId commit, @Nullable String message, @Nullable PersonIdent tagger, Repository repo) throws IOException {
    TagBuilder builder = new TagBuilder();
    builder.setTag(name);
    builder.setMessage(message);
    builder.setTagger(tagger);
    builder.setObjectId(commit, OBJ_COMMIT);
    AnyObjectId tag;
    try(ObjectInserter inserter = repo.newObjectInserter()) {
      tag = inserter.insert(builder);
      inserter.flush();
    }
    return linkTag(name, tag, repo);
  }

  @Nonnull
  public static Ref tagCommit(String name, AnyObjectId commit, @Nullable String message, Repository repo) throws IOException {
    return tagCommit(name, commit, message, new PersonIdent(repo), repo);
  }

  @Nonnull
  public static Ref tagCommit(String name, AnyObjectId commit, Repository repo) throws IOException {
    return tagCommit(name, commit, null, repo);
  }

  @Nonnull
  public static Ref tagCommit(String name, String revision, Repository repo) throws IOException {
    return tagCommit(name, repo.resolve(revision), repo);
  }

  @Nonnull
  public static Ref tagHeadCommit(String name, Repository repo) throws IOException {
    if(repo.isBare())
      throw new IllegalArgumentException("Bare repository does not have head commit");
    return tagCommit(name, Constants.HEAD, repo);
  }

  @Nonnull
  public static RevTag getTag(AnyObjectId tagObjectId, ObjectReader reader) throws IOException {
    try(RevWalk revWalk = new RevWalk(reader)) {
      return revWalk.parseTag(tagObjectId);
    }
  }

  @Nonnull
  public static RevTag getTag(Ref tagRef, ObjectReader reader) throws IOException {
    return getTag(tagRef.getObjectId(), reader);
  }

  @Nonnull
  public static RevTag getTag(Ref tagRef, Repository repo) throws IOException {
    try(ObjectReader reader = repo.newObjectReader()) {
      return getTag(tagRef, reader);
    }
  }

  @Nullable
  public static RevTag getTag(String tagName, Repository repo) throws IOException {
    Ref tagRef = repo.exactRef(RefUtils.fullTagRef(tagName));
    return tagRef != null ? getTag(tagRef, repo) : null;
  }

  @Nonnull
  private static Ref linkTag(String name, AnyObjectId tag, Repository repo) throws IOException {
    String refName = RefUtils.fullTagRef(name);
    if(tagExists(name, repo))
      throw new TagAlreadyExistsException(refName);
    RefUpdate update = repo.updateRef(refName);
    update.setNewObjectId(tag);
    update.setRefLogMessage("tagged " + name, false);
    RefUpdateValidator.validate(update.update());
    return repo.exactRef(refName);
  }

}
