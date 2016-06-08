package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommitUtilsGetFileRevisionsTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void getFileRevisions_shouldReturnTheCommitsWhereTheSpecifiedFileIsChanged() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    writeToCache("/test_file.txt");
    RevCommit commit2 = commitToBranch("test_branch");
    updateFile("/test_file.txt", "some new text data");
    RevCommit commit3 = commitToBranch("test_branch");
    writeSomethingToCache();
    commitToBranch("test_branch");

    List<RevCommit> expected = Arrays.asList(commit3, commit2);
    List<RevCommit> actual = CommitUtils.getFileRevisions("/test_file.txt", "test_branch", repo);
    assertEquals(expected, actual);
  }

  @Test
  public void getFileRevisionsWhenFileIsNeverChanged_shouldReturnTheCommitWhereTheSpecifiedFileIsCreated() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    writeToCache("/test_file.txt");
    RevCommit commit = commitToBranch("test_branch");
    writeSomethingToCache();
    commitToBranch("test_branch");

    List<RevCommit> revisions = CommitUtils.getFileRevisions("/test_file.txt", "test_branch", repo);
    assertEquals(1, revisions.size());
    assertEquals(commit, revisions.get(0));
  }

  @Test
  public void getFileRevisionsWhenFileNeverExisted_shouldReturnEmptyList() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");

    List<RevCommit> revisions = CommitUtils.getFileRevisions("/non_existent_file.txt", "test_branch", repo);
    assertTrue(revisions.isEmpty());
  }

  @Test
  public void getFileRevisionsWhenBranchDoesNotExist_shouldReturnEmptyList() throws IOException {
    List<RevCommit> revisions = CommitUtils.getFileRevisions("/non_existent_file.txt", "non_existent_branch", repo);
    assertTrue(revisions.isEmpty());
  }

  @Test
  public void getLatestFileRevision_shouldReturnTheLatestCommitThatChangedTheSpecificFile() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");
    writeToCache("/test_file.txt");
    commitToBranch("test_branch");
    updateFile("/test_file.txt", "some new text data");
    RevCommit expected = commitToBranch("test_branch");
    writeSomethingToCache();
    commitToBranch("test_branch");

    RevCommit actual = CommitUtils.getLatestFileRevision("/test_file.txt", "test_branch", repo);
    assertEquals(expected, actual);
  }

  @Test
  public void getLatestFileRevisionWhenFileNeverExisted_shouldReturnNull() throws IOException {
    writeSomethingToCache();
    commitToBranch("test_branch");

    assertNull(CommitUtils.getLatestFileRevision("/non_existent_file.txt", "test_branch", repo));
  }

  @Test
  public void getLatestFileRevisionWhenBranchDoesNotExist_shouldReturnNull() throws IOException {
    assertNull(CommitUtils.getLatestFileRevision("/non_existent_file.txt", "non_existent_branch", repo));
  }

}
