package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.ParallelGitException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public final class BranchHelper {

  /**
   * Checks if a branch with the given name exists.
   *
   * @param repo a git repository
   * @param name a branch name
   * @return true if branch with the given name exists
   */
  public static boolean existsBranch(@Nonnull Repository repo, @Nonnull String name) {
    Ref ref = RefHelper.getRef(repo, name);
    return ref != null && RefHelper.isBranchRef(ref);
  }

  /**
   * Gets the HEAD commit id of the given branch.
   *
   * @param repo a git repository
   * @param name a branch name
   * @return the HEAD commit id of the given branch
   */
  @Nullable
  public static ObjectId getBranchHeadCommitId(@Nonnull Repository repo, @Nonnull String name) {
    String refName = RefHelper.getBranchRefName(name);
    try {
      return repo.resolve(refName);
    } catch(IOException e) {
      throw new ParallelGitException("Could not get the HEAD commit of " + refName, e);
    }
  }

  /**
   * Creates a new branch with the given name based on the given revision as its start-point.
   * {@code CreateBranchCommand}
   *
   * @param repo a git repository
   * @param name the name of the new branch
   * @param revision a git revision as the start-point
   * @param force if {@code true} and the branch with given name already exists, the start-point of an existing branch will be set to a new start-point; if {@code false} and the branch with given name already exists, the operation will abort with an {@link com.beijunyi.parallelgit.ParallelGitException}
   * @return a ref update result
   */
  public static RefUpdate.Result createBranch(@Nonnull Repository repo, @Nonnull String name, @Nonnull String revision, boolean force) {
    String branchRef = RefHelper.getBranchRefName(name);
    boolean exists = existsBranch(repo, branchRef);
    if(exists && !force)
      throw new ParallelGitException("Branch " + name + " already exists");

    ObjectId revisionId = RepositoryHelper.getRevisionId(repo, revision);
    if(revisionId == null)
      throw new ParallelGitException("Could not find revision " + revision);

    Ref baseRef = RefHelper.getRef(repo, revision);

    RevWalk revWalk = new RevWalk(repo);
    String refLogMessage;
    if(baseRef == null) {
      RevCommit commit = CommitHelper.getCommit(revWalk, revisionId);
      refLogMessage = "branch: " + (exists ? "Reset start-point to commit" : "Created from commit") + " " + commit.getShortMessage();
    } else {
      if(RefHelper.isBranchRef(baseRef))
        refLogMessage = "branch: " + (exists ? "Reset start-point to branch" : "Created from branch") + " " + baseRef.getName();
      else if (RefHelper.isTagRef(baseRef)) {
        revisionId = RevWalkHelper.peel(revWalk, revisionId);
        refLogMessage = "branch: " + (exists ? "Reset start-point to tag" : "Created from tag") + " " + baseRef.getName();
      } else
        throw new ParallelGitException("Unknown ref " + baseRef);
    }
    RefUpdate update = RefHelper.updateRef(repo, branchRef);
    update.setNewObjectId(revisionId);
    update.setRefLogMessage(refLogMessage, false);
    update.setForceUpdate(force);
    return RefHelper.update(update);
  }

  @Nonnull
  public static RefUpdate.Result setBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId, @Nonnull String refLogMessage, boolean falseUpdate) {
    String refName = RefHelper.getBranchRefName(name);
    ObjectId currentHead = RepositoryHelper.getRevisionId(repo, refName);
    if(currentHead == null)
      currentHead = ObjectId.zeroId();

    RefUpdate ru = RefHelper.updateRef(repo, refName);
    ru.setRefLogMessage(refLogMessage, false);
    ru.setForceUpdate(falseUpdate);
    ru.setNewObjectId(commitId);
    ru.setExpectedOldObjectId(currentHead);
    return RefHelper.update(ru);
  }

  /**
   * Resets the {@code HEAD} of the specified branch to the given commit.
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result resetBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId) {
    return setBranchHead(repo, name, commitId, RefHelper.getBranchRefName(name) + ": updating " + Constants.HEAD, true);
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "commit: ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @param shortMessage the first line of the commit message
   * @param falseUpdate whether to false update the branch ref
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result commitBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId, @Nonnull String shortMessage, boolean falseUpdate) {
    return setBranchHead(repo, name, commitId, "commit: " + shortMessage, falseUpdate);
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "commit: ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @param shortMessage the first line of the commit message
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result commitBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId, @Nonnull String shortMessage) {
    return commitBranchHead(repo, name, commitId, shortMessage, false);
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "commit: ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result commitBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId) {
    return commitBranchHead(repo, name, commitId, CommitHelper.getCommit(repo, commitId).getShortMessage());
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "commit (amend): ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @param shortMessage the first line of the commit message
   * @param falseUpdate whether to false update the branch ref
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result amendBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId, @Nonnull String shortMessage, boolean falseUpdate) {
    return setBranchHead(repo, name, commitId, "commit (amend): " + shortMessage, falseUpdate);
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "commit (amend): ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @param shortMessage the first line of the commit message
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result amendBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId, @Nonnull String shortMessage) {
    return amendBranchHead(repo, name, commitId, shortMessage, false);
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "commit (amend): ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result amendBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId) {
    return amendBranchHead(repo, name, commitId, CommitHelper.getCommit(repo, commitId).getShortMessage());
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "cherry-pick: ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @param shortMessage the first line of the commit message
   * @param falseUpdate whether to false update the branch ref
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result cherryPickBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId, @Nonnull String shortMessage, boolean falseUpdate) {
    return setBranchHead(repo, name, commitId, "cherry-pick: " + shortMessage, falseUpdate);
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "cherry-pick: ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @param shortMessage the first line of the commit message
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result cherryPickBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId, @Nonnull String shortMessage) {
    return cherryPickBranchHead(repo, name, commitId, shortMessage, false);
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "cherry-pick: ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result cherryPickBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId) {
    return cherryPickBranchHead(repo, name, commitId, CommitHelper.getCommit(repo, commitId).getShortMessage());
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "commit (initial): ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @param shortMessage the first line of the commit message
   * @param falseUpdate whether to false update the branch ref
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result initBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId, @Nonnull String shortMessage, boolean falseUpdate) {
    return setBranchHead(repo, name, commitId, "commit (initial): " + shortMessage, falseUpdate);
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "commit (initial): ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @param shortMessage the first line of the commit message
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result initBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId, @Nonnull String shortMessage) {
    return initBranchHead(repo, name, commitId, shortMessage, false);
  }

  /**
   * Sets the {@code HEAD} of the specified branch to the given commit with {@code reflog} message starting with
   * "commit (initial): ".
   *
   * @param repo a git repository
   * @param name a branch name
   * @param commitId a commit id
   * @return a ref update result
   */
  @Nonnull
  public static RefUpdate.Result initBranchHead(@Nonnull Repository repo, @Nonnull String name, @Nonnull ObjectId commitId) {
    return initBranchHead(repo, name, commitId, CommitHelper.getCommit(repo, commitId).getShortMessage());
  }

  /**
   * Deletes the specified branch.
   *
   * {@code DeleteBranchCommand}
   *
   * @param repo a git repository
   * @param name the name of the branch to be deleted
   */
  public static void deleteBranch(@Nonnull Repository repo, @Nonnull String name) {
    String refName = RefHelper.getBranchRefName(name);
    RefUpdate update = RefHelper.updateRef(repo, refName);
    update.setRefLogMessage("branch deleted", false);
    update.setForceUpdate(true);
    RefHelper.delete(update);
  }

}
