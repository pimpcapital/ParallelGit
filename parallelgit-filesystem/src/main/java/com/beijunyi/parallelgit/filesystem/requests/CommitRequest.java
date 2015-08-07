package com.beijunyi.parallelgit.filesystem.requests;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.BranchHelper;
import com.beijunyi.parallelgit.utils.CommitHelper;
import com.beijunyi.parallelgit.utils.RefHelper;
import com.beijunyi.parallelgit.utils.exception.RefUpdateValidator;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.revwalk.RevCommit;

public final class CommitRequest extends GitFileSystemRequest<RevCommit> {

  private final String branchRef;
  private final RevCommit commit;
  private PersonIdent author;
  private String authorName;
  private String authorEmail;
  private PersonIdent committer;
  private String committerName;
  private String committerEmail;
  private String message;
  private List<AnyObjectId> parents;
  private boolean amend = false;
  private boolean allowEmpty = false;

  private CommitRequest(@Nonnull GitFileSystem gfs) {
    super(gfs);
    String branch = gfs.getBranch();
    branchRef = branch != null ? RefHelper.getBranchRefName(branch) : null;
    commit = gfs.getCommit();
  }

  @Nonnull
  static CommitRequest prepare(@Nonnull GitFileSystem gfs) {
    return new CommitRequest(gfs);
  }

  @Nonnull
  public CommitRequest author(@Nullable PersonIdent author) {
    this.author = author;
    return this;
  }

  @Nonnull
  public CommitRequest author(@Nullable String name, @Nullable String email) {
    this.authorName = name;
    this.authorEmail = email;
    return this;
  }

  @Nonnull
  public CommitRequest committer(@Nullable PersonIdent committer) {
    this.committer = committer;
    return this;
  }

  @Nonnull
  public CommitRequest committer(@Nullable String name, @Nullable String email) {
    this.committerName = name;
    this.committerEmail = email;
    return this;
  }

  @Nonnull
  public CommitRequest message(@Nullable String message) {
    this.message = message;
    return this;
  }

  public void parents(@Nonnull List<AnyObjectId> parents) {
    this.parents = parents;
  }

  public void parent(@Nullable AnyObjectId parent) {
    if(parent == null)
      parents = Collections.emptyList();
    else
      parents = Collections.singletonList(parent);
  }

  @Nonnull
  public CommitRequest amend(boolean amend) {
    this.amend = amend;
    return this;
  }

  @Nonnull
  public CommitRequest allowEmpty(boolean allowEmpty) {
    this.allowEmpty = allowEmpty;
    return this;
  }

  @Nonnull
  private RevCommit amendedCommit() {
    if(commit == null)
      throw new IllegalStateException("No commit to amend");
    return commit;
  }

  private void prepareCommitter() {
    if(committer == null) {
      if(committerName != null && committerEmail != null)
        committer = new PersonIdent(committerName, committerEmail);
      else if(committerName == null && committerEmail == null)
        committer = new PersonIdent(repository);
      else
        throw new IllegalStateException();
    }
  }

  private void prepareAuthor() {
    if(author == null) {
      if(!amend) {
        if(authorName != null && authorEmail != null)
          author = new PersonIdent(authorName, authorEmail);
        else if(authorName == null && authorEmail == null && committer != null)
          author = committer;
        else
          throw new IllegalStateException();
      } else {
        RevCommit amendedCommit = amendedCommit();
        PersonIdent amendedAuthor = amendedCommit.getAuthorIdent();
        author = new PersonIdent(authorName != null ? authorName : amendedAuthor.getName(),
                                  authorEmail != null ? authorEmail : amendedAuthor.getEmailAddress());
      }
    }
  }

  private void prepareParents() {
    if(parents == null) {
      if(!amend) {
        if(commit != null)
          parents = Collections.<AnyObjectId>singletonList(commit);
        else
          parents = Collections.emptyList();
      } else
        parents = Arrays.<AnyObjectId>asList(amendedCommit().getParents());
    }
  }

  private void updateRef(@Nonnull RevCommit head) throws IOException {
    RefUpdate.Result result;
    if(amend)
      result = BranchHelper.amendBranchHead(repository, branchRef, head);
    else if(commit != null)
      result = BranchHelper.commitBranchHead(repository, branchRef, head);
    else
      result = BranchHelper.initBranchHead(repository, branchRef, head);
    RefUpdateValidator.validate(branchRef, result);
  }

  private void updateFileSystem(@Nonnull RevCommit head) {
    gfs.setCommit(head);
  }

  @Nullable
  @Override
  public RevCommit doExecute() throws IOException {
    prepareCommitter();
    prepareAuthor();
    prepareParents();
    AnyObjectId tree = gfs.persist();
    if(!allowEmpty && !amend && tree.equals(commit.getTree()))
      return null;
    RevCommit result = CommitHelper.createCommit(repository, tree, author, committer, message, parents);
    updateRef(result);
    updateFileSystem(result);
    return result;
  }
}
