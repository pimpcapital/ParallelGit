package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.List;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.RepositoryUtils.setRefLogEnabled;
import static com.beijunyi.parallelgit.utils.StashUtils.*;

public class StashUtilsCreateStashTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws Exception {
    initFileRepository(true);
    setRefLogEnabled(true, repo);
  }

  @Test
  public void createStashAndListStashes_theFirstOneShouldBeTheNewStash() throws IOException {
    writeSomethingToCache();
    RevCommit expected = commit();
    createStash(expected, repo);
    List<RevCommit> stashes = listStashes(repo);
    Assert.assertEquals(expected, stashes.get(0));
  }


}
