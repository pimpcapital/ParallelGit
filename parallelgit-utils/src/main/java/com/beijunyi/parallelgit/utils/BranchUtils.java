package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exception.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

public final class BranchUtils {

  @Nonnull
  public static List<RevCommit> getBranchHistory(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    String branchRef = RefUtils.ensureBranchRefName(name);
    RevCommit head = CommitUtils.getCommit(branchRef, repo);
    if(head == null)
      throw new NoSuchBranchException(branchRef);
    return CommitUtils.getCommitHistory(head, repo);
  }

  public static boolean branchExists(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    Ref ref = repo.getRef(RefUtils.ensureBranchRefName(name));
    return ref != null;
  }

  @Nonnull
  public static AnyObjectId getBranchHeadCommit(@Nonnull String name, @Nonnull Repository repo) throws IOException {
    String refName = RefUtils.ensureBranchRefName(name);
    AnyObjectId ret =  repo.resolve(refName);
    if(ret == null)
      throw new NoSuchBranchException(refName);
    return ret;
  }

  public static void createBranch(@Nonnull String name, @Nonnull AnyObjectId startPoint, @Nonnull Repository repo, @Nullable String refLogMessage) throws IOException {
    String branchRef = RefUtils.ensureBranchRefName(name);
    if(branchExists(branchRef, repo))
      throw new BranchAlreadyExistsException(branchRef);
    setBranchHead(name, startPoint, repo, refLogMessage, false);
  }

  public static void createBranch(@Nonnull String name, @Nonnull String startPoint, @Nonnull Repository repo) throws IOException {
    Ref ref = repo.getRef(startPoint);
    AnyObjectId commitId;
    String refLogMessage = "branch: Created from ";
    if(ref != null) {
      if(RefUtils.isBranchRef(ref)) {
        commitId = ref.getObjectId();
        refLogMessage += "branch " + ref.getName();
      } else if(RefUtils.isTagRef(ref)) {
        commitId = TagUtils.getTaggedCommit(ref.getName(), repo);
        refLogMessage += "tag " + ref.getName();
      } else {
        throw new UnsupportedOperationException();
      }
    } else {
      commitId = repo.resolve(startPoint);
      if(commitId == null)
        throw new NoSuchRevisionException(startPoint);
      refLogMessage += "commit " + getShortMessage(commitId, repo);
    }
    createBranch(name, commitId, repo, refLogMessage);
  }

  public static void resetBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    setBranchHead(name, commitId, repo, RefUtils.ensureBranchRefName(name) + ": updating " + Constants.HEAD, true);
  }

  public static void updateBranchHead(@Nonnull String name, @Nonnull AnyObjectId commitId, @Nonnull Repository repo, @Nonnull BranchUpdateType type) throws IOException {
    setBranchHead(name, commitId, repo, type.getHeader() + getShortMessage(commitId, repo), type.isForceUpdate());
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

  @Nonnull
  private static String getShortMessage(@Nonnull AnyObjectId commitId, @Nonnull Repository repo) throws IOException {
    String ret = null;
    if(commitId instanceof RevCommit)
      ret = ((RevCommit) commitId).getShortMessage();
    return ret != null ? ret : CommitUtils.getCommit(commitId, repo).getShortMessage();
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
