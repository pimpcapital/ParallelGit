package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import static java.util.Collections.unmodifiableList;
import static org.eclipse.jgit.lib.Constants.R_STASH;
import static org.eclipse.jgit.lib.ObjectId.zeroId;

public final class StashUtils {

  public static void addToStash(RevCommit commit, Repository repo) throws IOException {
    RefUpdate update = repo.updateRef(R_STASH);
    update.setNewObjectId(commit);
    update.setRefLogIdent(commit.getCommitterIdent());
    AnyObjectId prevStash = repo.resolve(R_STASH);
    update.setExpectedOldObjectId(prevStash != null ? prevStash : zeroId());
    update.forceUpdate();
  }

  @Nonnull
  public static List<RevCommit> listStashes(Repository repo) throws IOException {
    List<RevCommit> ret = new ArrayList<>();
    List<ReflogEntry> logs = RefUtils.getRefLogs(R_STASH, Integer.MAX_VALUE, repo);
    try(RevWalk rw = new RevWalk(repo)) {
      for(ReflogEntry log : logs) ret.add(rw.parseCommit(log.getNewId()));
    }
    return unmodifiableList(ret);
  }

}
