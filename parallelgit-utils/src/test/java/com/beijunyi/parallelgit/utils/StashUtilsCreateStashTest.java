package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.List;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.RepositoryUtils.setRefLogEnabled;
import static com.beijunyi.parallelgit.utils.StashUtils.*;
import static org.junit.Assert.assertEquals;

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
    assertEquals(expected, stashes.get(0));
  }

  @Test
  public void createTwoStashes_listStashesShouldReturnTheStashesInReversedOrder() throws IOException {
    writeSomethingToCache();
    RevCommit first = commit();
    createStash(first, repo);

    writeSomethingToCache();
    RevCommit second = commit();
    createStash(second, repo);

    List<RevCommit> stashes = listStashes(repo);
    assertEquals(second, stashes.get(0));
    assertEquals(first, stashes.get(1));
  }


}
