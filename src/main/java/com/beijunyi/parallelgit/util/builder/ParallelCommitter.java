package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.util.CommitHelper;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;

public final class ParallelCommitter extends CacheBasedBuilder<ParallelCommitter, ObjectId> {
  private AnyObjectId treeId;
  private DirCache cache;
  private PersonIdent author;
  private String authorName;
  private String authorEmail;
  private PersonIdent committer;
  private String committerName;
  private String committerEmail;
  private String message;
  private List<AnyObjectId> parents;

  private ParallelCommitter(@Nonnull Repository repository) {
    super(repository);
  }

  @Nonnull
  @Override
  protected ParallelCommitter self() {
    return this;
  }

  @Nonnull
  public ParallelCommitter setTreeId(@Nullable AnyObjectId treeId) {
    this.treeId = treeId;
    return this;
  }

  @Nonnull
  public ParallelCommitter setCache(@Nullable DirCache cache) {
    this.cache = cache;
    return this;
  }

  private void ensureContent(@Nonnull ObjectInserter inserter) throws IOException {
    if(treeId == null && cache == null)
      throw new IllegalArgumentException("either of treeId or cache must be configured");
    if(treeId == null) {
      treeId = cache.writeTree(inserter);
      inserter.flush();
    }
  }

  @Nonnull
  public ParallelCommitter setAuthor(@Nullable PersonIdent author) {
    this.author = author;
    return this;
  }

  @Nonnull
  public ParallelCommitter setAuthorName(@Nullable String authorName) {
    this.authorName = authorName;
    return this;
  }

  @Nonnull
  public ParallelCommitter setAuthorEmail(@Nullable String authorEmail) {
    this.authorEmail = authorEmail;
    return this;
  }

  private void ensureAuthor() {
    if(author == null) {
      if(authorName == null && authorEmail == null)
        throw new IllegalArgumentException("author must be configured");
      if(authorName == null)
        throw new IllegalArgumentException("missing authorName");
      if(authorEmail == null)
        throw new IllegalArgumentException("missing authorEmail");
      author = new PersonIdent(authorName, authorEmail);
    }
  }

  @Nonnull
  public ParallelCommitter setCommitter(@Nullable PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public ParallelCommitter setCommitterName(@Nullable String committerName) {
    this.committerName = committerName;
    return this;
  }

  @Nonnull
  public ParallelCommitter setCommitterEmail(@Nullable String committerEmail) {
    this.committerEmail = committerEmail;
    return this;
  }

  private void ensureCommitter() {
    if(committer == null) {
      if(committerName == null && committerEmail == null) {
        if(author == null)
          throw new IllegalStateException("missing author");
        committer = author;
        return;
      }
      if(committerName == null)
        throw new IllegalArgumentException("missing committerName");
      if(committerEmail == null)
        throw new IllegalArgumentException("missing committerEmail");
      committer = new PersonIdent(committerName, committerEmail);
    }
  }

  @Nonnull
  public ParallelCommitter setMessage(@Nullable String message) {
    this.message = message;
    return this;
  }

  private void ensureMessage() {
    if(message == null)
      throw new IllegalArgumentException("message must be configured");
    if(message.isEmpty())
      throw new IllegalArgumentException("message must not be empty");
  }

  @Nonnull
  public ParallelCommitter setParents(@Nullable List<AnyObjectId> parents) {
    this.parents = parents;
    return this;
  }

  @Nonnull
  public ParallelCommitter addParent(@Nonnull AnyObjectId parent) {
    if(parents == null)
      parents = new ArrayList<>();
    parents.add(parent);
    return this;
  }

  @Nonnull
  public ParallelCommitter addParent(@Nonnull AnyObjectId parent, int index) {
    if(parents == null)
      parents = new ArrayList<>();
    parents.add(index, parent);
    return this;
  }

  private void ensureParents() {
    if(parents != null) {
      Set<AnyObjectId> checkSet = new HashSet<>();
      for(AnyObjectId parent : parents) {
        if(parent == null)
          throw new IllegalArgumentException("parent must not be null");
        if(!checkSet.add(parent))
          throw new IllegalArgumentException("duplicate parent " + parent);
      }
    }
  }

  @Nullable
  @Override
  public ObjectId doBuild() throws IOException {
    assert repository != null;
    ObjectInserter inserter = repository.newObjectInserter();
    try {
      ensureContent(inserter);
      ensureAuthor();
      ensureCommitter();
      ensureMessage();
      ensureParents();
      ObjectId commit = CommitHelper.createCommit(inserter, treeId, author, committer, message, parents);
      inserter.flush();
      return commit;
    } finally {
      inserter.release();
    }
  }

  @Nonnull
  public static ParallelCommitter prepare(@Nonnull Repository repository) {
    return new ParallelCommitter(repository);
  }

}