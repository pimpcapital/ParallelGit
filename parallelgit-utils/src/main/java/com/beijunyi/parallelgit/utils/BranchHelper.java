package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public final class BranchHelper {

  @Nonnull
  public static List<RevCommit> getBranchHistory(@Nonnull Repository repo, @Nonnull String name) throws IOException {
    String branchRef = RefHelper.getBranchRefName(name);
    RevCommit head = CommitHelper.getCommit(repo, branchRef);
    if(head == null)
      throw new IllegalArgumentException("Branch " + name + " does not exist");
    return CommitHelper.getCommitHistory(repo, head);
  }

  public static boolean existsBranch(@Nonnull Repository repo, @Nonnull String name) throws IOException {
    Ref ref = repo.getRef(name);
    return ref != null && RefHelper.isBranchRef(ref);
  }

  @Nullable
  public static AnyObjectId getBranchHeadCommitId(@Nonnull Repository repo, @Nonnull String name) throws IOException {
    return repo.resolve(RefHelper.getBranchRefName(name));
  }

  @Nonnull
  public static RefUpdate.Result createBranch(@Nonnull Repository repo, @Nonnull String name, @Nonnull String revision, boolean force) throws IOException {
    String branchRef = RefHelper.getBranchRefName(name);
    boolean exists = existsBranch(repo, branchRef);
    if(exists && !force)
      throw new IllegalArgumentException("Branch " + name + " already exists");

    AnyObjectId revisionId = repo.resolve(revision);
    if(revisionId == null)
      throw new IllegalArgumentException("Could not find revision " + revision);

    Ref baseRef = repo.getRef(revision);

    RevWalk revWalk = new RevWalk(repo);
    String refLogMessage;
    if(baseRef == null) {
      RevCommit commit = revWalk.parseCommit(revisionId);
      refLogMessage = "branch: " + (exists ? "Reset start-point to commit" : "Created from commit") + " " + commit.getShortMessage();
    } else {
      if(RefHelper.isBranchRef(baseRef))
        refLogMessage = "branch: " + (exists ? "Reset start-point to branch" : "Created from branch") + " " + baseRef.getName();
      else if (RefHelper.isTagRef(baseRef)) {
        revisionId = revWalk.peel(revWalk.parseAny(revisionId));
        refLogMessage = "branch: " + (exists ? "Reset start-point to tag" : "Created from tag") + " " + baseRef.getName();
      } else
        throw new IllegalArgumentException("Unknown ref " + baseRef);
    }
    RefUpdate update = repo.updateRef(branchRef);
    update.setNewObjectId(revisionId);
    update.setRefLogMessage(refLogMessage, false);
    update.setForceUpdate(force);
    return update.update();
  }

  @Nonnull
  public static RefUpdate.Result setBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull String refLogMessage, boolean forceUpdate) throws IOException {
    String refName = RefHelper.getBranchRefName(name);
    AnyObjectId currentHead = repo.resolve(refName);
    if(currentHead == null)
      currentHead = ObjectId.zeroId();

    RefUpdate ru = repo.updateRef(refName);
    ru.setRefLogMessage(refLogMessage, false);
    ru.setForceUpdate(forceUpdate);
    ru.setNewObjectId(commitId);
    ru.setExpectedOldObjectId(currentHead);
    return ru.update();
  }

  @Nonnull
  public static RefUpdate.Result resetBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId) throws IOException {
    return setBranchHead(repo, name, commitId, RefHelper.getBranchRefName(name) + ": updating " + Constants.HEAD, true);
  }

  @Nonnull
  public static RefUpdate.Result commitBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull String shortMessage) throws IOException {
    return setBranchHead(repo, name, commitId, "commit: " + shortMessage, true);
  }

  @Nonnull
  public static RefUpdate.Result commitBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId) throws IOException {
    return commitBranchHead(repo, name, commitId, CommitHelper.getCommit(repo, commitId).getShortMessage());
  }

  @Nonnull
  public static RefUpdate.Result amendBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull String shortMessage) throws IOException {
    return setBranchHead(repo, name, commitId, "commit (amend): " + shortMessage, true);
  }

  @Nonnull
  public static RefUpdate.Result amendBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId) throws IOException {
    return amendBranchHead(repo, name, commitId, CommitHelper.getCommit(repo, commitId).getShortMessage());
  }

  @Nonnull
  public static RefUpdate.Result cherryPickBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull String shortMessage) throws IOException {
    return setBranchHead(repo, name, commitId, "cherry-pick: " + shortMessage, true);
  }

  @Nonnull
  public static RefUpdate.Result cherryPickBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId) throws IOException {
    return cherryPickBranchHead(repo, name, commitId, CommitHelper.getCommit(repo, commitId).getShortMessage());
  }

  @Nonnull
  public static RefUpdate.Result initBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull String shortMessage, boolean falseUpdate) throws IOException {
    return setBranchHead(repo, name, commitId, "commit (initial): " + shortMessage, falseUpdate);
  }

  @Nonnull
  public static RefUpdate.Result initBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull String shortMessage) throws IOException {
    return initBranchHead(repo, name, commitId, shortMessage, false);
  }

  @Nonnull
  public static RefUpdate.Result initBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull AnyObjectId commitId) throws IOException {
    return initBranchHead(repo, name, commitId, CommitHelper.getCommit(repo, commitId).getShortMessage());
  }

  @Nonnull
  public static RefUpdate.Result deleteBranch(@Nonnull Repository repo, @Nonnull String name) throws IOException {
    String refName = RefHelper.getBranchRefName(name);
    RefUpdate update = repo.updateRef(refName);
    update.setRefLogMessage("branch deleted", false);
    update.setForceUpdate(true);
    return update.delete();
  }

  public static enum UpdateType {
    COMMIT,
    AMEND,
    INIT,
    CHERRYPICK,
    MERGE
  }

}
