package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exceptions.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;

public final class BranchUtils {

  @Nonnull
  public static List<RevCommit> getBranchHistory(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    String branchRef = RefUtils.ensureBranchRefName(name);
    RevCommit head = CommitUtils.getCommit(branchRef, repo);
    if(head == null)
      throw new NoSuchBranchException(branchRef);
    return CommitUtils.getCommitHistory(head, repo);
  }

  @Nonnull
  public static Map<String, Ref> getBranches(@Nonnull Repository repo) throws IOException {
    return repo.getRefDatabase().getRefs(Constants.R_HEADS);
  }

  public static boolean branchExists(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    Ref ref = repo.getRef(RefUtils.ensureBranchRefName(name));
    return ref != null;
  }

  @Nonnull
  public static AnyObjectId getBranchHeadCommit(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    Ref ref = RefUtils.getBranchRef(name, repo);
    if(ref == null)
      throw new NoSuchBranchException(name);
    return ref.getObjectId();
  }

  public static void createBranch(@Nonnull String name, @Nonnull RevTag startPoint, @Nonnull Repository repo) throws IOException {
    createBranch(name, startPoint.getObject(), repo, "tag " + startPoint.getName());
  }

  public static void createBranch(@Nonnull String name, @Nonnull RevCommit startPoint, @Nonnull Repository repo) throws IOException {
    createBranch(name, startPoint, repo, "commit " + startPoint.getShortMessage());
  }

  public static void createBranch(@Nonnull String name, @Nonnull AnyObjectId startPoint, @Nonnull Repository repo) throws IOException {
    try(RevWalk rw = new RevWalk(repo)) {
      RevObject revObj = rw.parseAny(startPoint);
      switch(revObj.getType()) {
        case Constants.OBJ_TAG:
          createBranch(name, (RevTag) revObj, repo);
          break;
        case Constants.OBJ_COMMIT:
          createBranch(name, (RevCommit) revObj, repo);
          break;
        default:
          throw new UnsupportedOperationException(revObj.getName());
      }
    }
  }

  public static void createBranch(@Nonnull String name, @Nonnull Ref startPoint, @Nonnull Repository repo) throws IOException {
    if(RefUtils.isBranchRef(startPoint))
      createBranch(name, startPoint.getObjectId(), repo, "branch " + startPoint.getName());
    else if(RefUtils.isTagRef(startPoint))
      createBranch(name, startPoint.getObjectId(), repo);
    else
      throw new UnsupportedOperationException(startPoint.getName());
  }

  public static void createBranch(@Nonnull String name, @Nonnull String startPoint, @Nonnull Repository repo) throws IOException {
    Ref ref = repo.getRef(startPoint);
    if(ref != null)
      createBranch(name, ref, repo);
    else {
      RevCommit commit = CommitUtils.getCommit(startPoint, repo);
      if(commit == null)
        throw new NoSuchRevisionException(startPoint);
      createBranch(name, commit, repo);
    }
  }

  public static void resetBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    setBranchHead(name, commitId, repo, RefUtils.ensureBranchRefName(name) + ": updating " + Constants.HEAD, true);
  }

  public static void updateBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo, @Nonnull BranchUpdateType type) throws IOException {
    setBranchHead(name, commitId, repo, type.getHeader() + CommitUtils.getCommit(commitId, repo).getShortMessage(), type.isForceUpdate());
  }

  public static void newCommit(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    updateBranchHead(name, commitId, repo, BranchUpdateType.COMMIT);
  }

  public static void amendCommit(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    updateBranchHead(name, commitId, repo, BranchUpdateType.COMMIT_AMEND);
  }

  public static void cherryPickCommit(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    updateBranchHead(name, commitId, repo, BranchUpdateType.CHERRY_PICK);
  }

  public static void initBranch(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    updateBranchHead(name, commitId, repo, BranchUpdateType.COMMIT_INIT);
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

  private static void setBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo, @Nullable String refLogMessage, boolean forceUpdate) throws IOException {
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

  private static void createBranch(@Nonnull String name, @Nonnull AnyObjectId startPoint, @Nonnull Repository repo, @Nonnull String startPointName) throws IOException {
    String branchRef = RefUtils.ensureBranchRefName(name);
    if(branchExists(branchRef, repo))
      throw new BranchAlreadyExistsException(branchRef);
    setBranchHead(name, startPoint, repo, "branch: Created from " + startPointName, false);
  }

  private static boolean prepareDeleteBranch(@Nonnull String refName, @Nonnull Repository repo) throws IOException {
    boolean branchExists = branchExists(refName, repo);
    if(refName.equals(repo.getFullBranch())) {
      if(branchExists)
        RepositoryUtils.detachRepositoryHead(repo, repo.resolve(refName));
      else
        return false;
    } else if(!branchExists)
      throw new NoSuchBranchException(refName);
    return true;
  }

}
