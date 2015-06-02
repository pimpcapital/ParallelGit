package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.util.CommitHelper;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;

public final class ParallelCommitter extends ParallelBuilder<ObjectId> {
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

  public void setTreeId(@Nullable AnyObjectId treeId) {
    this.treeId = treeId;
  }

  public void setCache(@Nullable DirCache cache) {
    this.cache = cache;
  }

  private void ensureContent(@Nonnull ObjectInserter inserter) throws IOException {
    if(treeId == null && cache == null)
      throw new IllegalArgumentException("either of treeId or cache must be configured");
    if(treeId == null) {
      treeId = cache.writeTree(inserter);
      inserter.flush();
    }
  }

  public void setAuthor(@Nullable PersonIdent author) {
    this.author = author;
  }

  public void setAuthorName(@Nullable String authorName) {
    this.authorName = authorName;
  }

  public void setAuthorEmail(@Nullable String authorEmail) {
    this.authorEmail = authorEmail;
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

  public void setCommitter(@Nullable PersonIdent committer) {
    this.committer = committer;
  }

  public void setCommitterName(@Nullable String committerName) {
    this.committerName = committerName;
  }

  public void setCommitterEmail(@Nullable String committerEmail) {
    this.committerEmail = committerEmail;
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

  public void setMessage(@Nullable String message) {
    this.message = message;
  }

  private void ensureMessage() {
    if(message == null)
      throw new IllegalArgumentException("message must be configured");
    if(message.isEmpty())
      throw new IllegalArgumentException("message must not be empty");
  }

  public void setParents(@Nullable List<AnyObjectId> parents) {
    this.parents = parents;
  }

  public void addParent(@Nonnull AnyObjectId parent) {
    if(parents == null)
      parents = new ArrayList<>();
    parents.add(parent);
  }

  public void addParent(@Nonnull AnyObjectId parent, int index) {
    if(parents == null)
      parents = new ArrayList<>();
    parents.add(index, parent);
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
}