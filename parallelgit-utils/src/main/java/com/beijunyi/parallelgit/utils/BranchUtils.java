package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exception.NoSuchRefException;
import com.beijunyi.parallelgit.utils.exception.RefUpdateValidator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public final class BranchUtils {

  @Nonnull
  public static List<RevCommit> getBranchHistory(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    String branchRef = RefUtils.ensureBranchRefName(name);
    RevCommit head = CommitUtils.getCommit(branchRef, repo);
    if(head == null)
      throw new NoSuchRefException(branchRef);
    return CommitUtils.getCommitHistory(repo, head);
  }

  public static boolean existsBranch(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    Ref ref = repo.getRef(RefUtils.ensureBranchRefName(name));
    return ref != null;
  }

  @Nullable
  public static AnyObjectId getBranchHeadCommit(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    return repo.resolve(RefUtils.ensureBranchRefName(name));
  }

  @Nonnull
  public static RefUpdate.Result createBranch(@Nonnull String name, @Nonnull String revision, @Nonnull Repository repo, boolean force) throws IOException {
    String branchRef = RefUtils.ensureBranchRefName(name);
    boolean exists = existsBranch(branchRef, repo);
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
      if(RefUtils.isBranchRef(baseRef))
        refLogMessage = "branch: " + (exists ? "Reset start-point to branch" : "Created from branch") + " " + baseRef.getName();
      else if (RefUtils.isTagRef(baseRef)) {
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

  public static void setBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo, @Nonnull String refLogMessage, boolean forceUpdate) throws IOException {
    String refName = RefUtils.ensureBranchRefName(name);
    AnyObjectId currentHead = repo.resolve(refName);
    if(currentHead == null)
      currentHead = ObjectId.zeroId();

    RefUpdate update = repo.updateRef(refName);
    update.setRefLogMessage(refLogMessage, false);
    update.setForceUpdate(forceUpdate);
    update.setNewObjectId(commitId);
    update.setExpectedOldObjectId(currentHead);
    RefUpdateValidator.validate(update.update());
  }

  public static void resetBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    setBranchHead(name, commitId, repo, RefUtils.ensureBranchRefName(name) + ": updating " + Constants.HEAD, true);
  }

  public static void commitBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo, @Nonnull String shortMessage) throws IOException {
    setBranchHead(name, commitId, repo, "commit: " + shortMessage, false);
  }

  public static void commitBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    commitBranchHead(name, commitId, repo, CommitUtils.getCommit(commitId, repo).getShortMessage());
  }

  public static void amendBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo, @Nonnull String shortMessage) throws IOException {
    setBranchHead(name, commitId, repo, "commit (amend): " + shortMessage, true);
  }

  public static void amendBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    amendBranchHead(name, commitId, repo, CommitUtils.getCommit(commitId, repo).getShortMessage());
  }

  public static void cherryPickBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo, @Nonnull String shortMessage) throws IOException {
    setBranchHead(name, commitId, repo, "cherry-pick: " + shortMessage, false);
  }

  public static void cherryPickBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    cherryPickBranchHead(name, commitId, repo, CommitUtils.getCommit(commitId, repo).getShortMessage());
  }

  public static void initBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo, @Nonnull String shortMessage) throws IOException {
    setBranchHead(name, commitId, repo, "commit (initial): " + shortMessage, false);
  }

  public static void initBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    initBranchHead(name, commitId, repo, CommitUtils.getCommit(commitId, repo).getShortMessage());
  }

  private static boolean prepareDeleteBranch(@Nonnull String refName, @Nonnull Repository repo) throws IOException {
    boolean branchExists = existsBranch(refName, repo);
    if(refName.equals(repo.getFullBranch())) {
      if(branchExists)
        RepositoryUtils.detachRepositoryHead(repo, repo.resolve(refName));
      else
        return false;
    } else if(!branchExists)
      throw new NoSuchRefException(refName);
    return true;
  }

  public static void deleteBranch(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    String refName = RefUtils.ensureBranchRefName(name);
    if(prepareDeleteBranch(refName, repo)) {
      RefUpdate update = repo.updateRef(refName);
      update.setRefLogMessage("branch deleted", false);
      update.setForceUpdate(true);
      RefUpdateValidator.validate(update.delete());
    }
  }

}
