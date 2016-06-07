package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.exceptions.BranchAlreadyExistsException;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchBranchException;
import com.beijunyi.parallelgit.utils.exceptions.RefUpdateValidator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;

import static com.beijunyi.parallelgit.utils.RefUtils.ensureBranchRefName;

public final class BranchUtils {

  @Nonnull
  public static List<RevCommit> getHistory(String name, Repository repo) throws IOException {
    Ref branchRef = repo.getRef(ensureBranchRefName(name));
    if(branchRef == null)
      throw new NoSuchBranchException(name);
    RevCommit head = CommitUtils.getCommit(branchRef, repo);
    return CommitUtils.getCommitHistory(head, repo);
  }

  @Nonnull
  public static Map<String, Ref> getBranches(Repository repo) throws IOException {
    return repo.getRefDatabase().getRefs(Constants.R_HEADS);
  }

  public static boolean branchExists(String name, Repository repo) throws IOException {
    Ref ref = repo.getRef(ensureBranchRefName(name));
    return ref != null;
  }

  @Nonnull
  public static RevCommit getHeadCommit(String name, Repository repo) throws IOException {
    Ref ref = RefUtils.getBranchRef(name, repo);
    if(ref == null)
      throw new NoSuchBranchException(name);
    return CommitUtils.getCommit(ref.getObjectId(), repo);
  }

  public static void createBranch(String name, RevTag startPoint, Repository repo) throws IOException {
    createBranch(name, startPoint.getObject(), repo, "tag " + startPoint.getName());
  }

  public static void createBranch(String name, RevCommit startPoint, Repository repo) throws IOException {
    createBranch(name, startPoint, repo, "commit " + startPoint.getShortMessage());
  }

  public static void createBranch(String name, AnyObjectId startPoint, Repository repo) throws IOException {
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

  public static void createBranch(String name, Ref startPoint, Repository repo) throws IOException {
    if(RefUtils.isBranchRef(startPoint))
      createBranch(name, startPoint.getObjectId(), repo, "branch " + startPoint.getName());
    else if(RefUtils.isTagRef(startPoint))
      createBranch(name, startPoint.getObjectId(), repo);
    else
      throw new UnsupportedOperationException(startPoint.getName());
  }

  public static void createBranch(String name, String startPoint, Repository repo) throws IOException {
    Ref ref = repo.getRef(startPoint);
    if(ref != null)
      createBranch(name, ref, repo);
    else {
      RevCommit commit = CommitUtils.getCommit(startPoint, repo);
      createBranch(name, commit, repo);
    }
  }

  public static void resetBranchHead(String name, AnyObjectId commitId, Repository repo) throws IOException {
    setBranchHead(name, commitId, repo, makeRefLogMessage(ensureBranchRefName(name), "updating HEAD"), true);
  }

  public static void newCommit(String name, AnyObjectId commitId, Repository repo) throws IOException {
    setBranchHead(name, commitId, repo, makeRefLogMessage("commit", commitId, repo), false);
  }

  public static void amendCommit(String name, AnyObjectId commitId, Repository repo) throws IOException {
    setBranchHead(name, commitId, repo, makeRefLogMessage("commit (amend)", commitId, repo), true);
  }

  public static void initBranch(String name, AnyObjectId commitId, Repository repo) throws IOException {
    setBranchHead(name, commitId, repo, makeRefLogMessage("commit (initial)", commitId, repo), false);
  }

  public static void cherryPickCommit(String name, AnyObjectId commitId, Repository repo) throws IOException {
    setBranchHead(name, commitId, repo, makeRefLogMessage("cherry-pick", commitId, repo), false);
  }

  public static void mergeBranch(String name, AnyObjectId commitId, Ref targetRef, String details, Repository repo) throws IOException {
    setBranchHead(name, commitId, repo, makeRefLogMessage("merge " + targetRef.getName(), details), false);
  }

  public static void deleteBranch(String name, Repository repo) throws IOException {
    String refName = ensureBranchRefName(name);
    if(prepareDeleteBranch(refName, repo)) {
      RefUpdate update = repo.updateRef(refName);
      update.setRefLogMessage("branch deleted", false);
      update.setForceUpdate(true);
      RefUpdateValidator.validate(update.delete());
    }
  }

  @Nonnull
  public static List<ReflogEntry> getLogs(String name, int max, Repository repository) throws IOException{
    return RefUtils.getRefLogs(ensureBranchRefName(name), max, repository);
  }

  @Nonnull
  public static List<ReflogEntry> getLogs(String name, Repository repository) throws IOException{
    return RefUtils.getRefLogs(ensureBranchRefName(name), Integer.MAX_VALUE, repository);
  }

  @Nullable
  public static ReflogEntry getLastLog(String name, Repository repository) throws IOException {
    List<ReflogEntry> entries = getLogs(name, 1, repository);
    if(entries.isEmpty())
      return null;
    return entries.get(0);
  }

  private static void setBranchHead(String name, AnyObjectId commitId, Repository repo, @Nullable String refLogMessage, boolean forceUpdate) throws IOException {
    String refName = ensureBranchRefName(name);
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

  private static void createBranch(String name, AnyObjectId startPoint, Repository repo, String startPointName) throws IOException {
    String branchRef = ensureBranchRefName(name);
    if(branchExists(branchRef, repo))
      throw new BranchAlreadyExistsException(branchRef);
    setBranchHead(name, startPoint, repo, makeRefLogMessage("branch", "Created from " + startPointName), false);
  }

  private static boolean prepareDeleteBranch(String refName, Repository repo) throws IOException {
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

  @Nonnull
  private static String makeRefLogMessage(String action, String details) {
    return action + ": " + details;
  }

  @Nonnull
  private static String makeRefLogMessage(String action, AnyObjectId commit, Repository repo) throws IOException {
    return makeRefLogMessage(action, CommitUtils.getCommit(commit, repo).getShortMessage());
  }

}
