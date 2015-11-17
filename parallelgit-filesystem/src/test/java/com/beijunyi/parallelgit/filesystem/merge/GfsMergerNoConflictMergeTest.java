package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@Ignore
public class GfsMergerNoConflictMergeTest extends AbstractParallelGitTest {

  private GfsMerger merger;

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void whenMergeFinishesWithNoConflict_shouldReturnTrue() throws IOException {
    AnyObjectId base = commit(null);

    clearCache();
    writeToCache("/ours.txt");
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/theirs.txt");
    AnyObjectId theirs = commit(base);

    assertTrue(merger.merge(ours, theirs));
  }

  @Test
  public void whenMergeFinishesSuccessfully_resultTreeIdShouldBeNotNull() throws IOException {
    AnyObjectId base = commit(null);

    clearCache();
    writeToCache("/ours.txt");
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/theirs.txt");
    AnyObjectId theirs = commit(base);

    merger.merge(ours, theirs);
    assertNotNull(merger.getResultTreeId());
  }

  @Test
  public void whenOurCommitAndTheirCommitInsertDifferentFiles_bothFilesShouldExistInTheResultTree() throws IOException {
    AnyObjectId base = commit(null);

    clearCache();
    writeToCache("/ours.txt");
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/theirs.txt");
    AnyObjectId theirs = commit(base);

    merger.merge(ours, theirs);
    AnyObjectId tree = merger.getResultTreeId();
    assertTrue(TreeUtils.exists("/ours.txt", tree, repo));
    assertTrue(TreeUtils.exists("/theirs.txt", tree, repo));
  }

  @Test
  public void whenOurCommitAndTheirCommitInsertDifferentFilesInOneDirectory_bothFilesShouldExistInTheResultTree() throws IOException {
    AnyObjectId base = commit(null);

    clearCache();
    writeToCache("/dir/ours.txt");
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/dir/theirs.txt");
    AnyObjectId theirs = commit(base);

    merger.merge(ours, theirs);
    AnyObjectId tree = merger.getResultTreeId();
    assertTrue(TreeUtils.exists("/dir/ours.txt", tree, repo));
    assertTrue(TreeUtils.exists("/dir/theirs.txt", tree, repo));
  }

  @Test
  public void whenOurAndTheirHaveTheSameInsertion_theFileShouldExistInTheResultTree() throws IOException {
    AnyObjectId base = commit(null);
    byte[] bytes = "same insertion".getBytes();

    clearCache();
    writeToCache("/same_insertion.txt", bytes);
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/same_insertion.txt", bytes);
    AnyObjectId theirs = commit(base);

    merger.merge(ours, theirs);
    AnyObjectId tree = merger.getResultTreeId();
    assertTrue(TreeUtils.exists("/same_insertion.txt", tree, repo));
  }

  @Test
  public void whenOursAndTheirsInsertTheSameFile_theFileShouldExistInTheResultTree() throws IOException {
    AnyObjectId base = commit(null);
    byte[] bytes = "same insertion".getBytes();

    clearCache();
    writeToCache("/same_insertion.txt", bytes);
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/same_insertion.txt", bytes);
    AnyObjectId theirs = commit(base);

    merger.merge(ours, theirs);
    AnyObjectId tree = merger.getResultTreeId();
    assertTrue(TreeUtils.exists("/same_insertion.txt", tree, repo));
  }

  @Test
  public void whenOursAndTheirsInsertTheSameDirectory_theDirectoryShouldExistInTheResultTree() throws IOException {
    AnyObjectId base = commit(null);
    byte[] bytes = "same insertion".getBytes();

    clearCache();
    writeToCache("/dir/same_insertion.txt", bytes);
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/dir/same_insertion.txt", bytes);
    AnyObjectId theirs = commit(base);

    merger.merge(ours, theirs);
    AnyObjectId tree = merger.getResultTreeId();
    assertTrue(TreeUtils.exists("/dir", tree, repo));
  }

  @Test
  public void whenOurAndTheirHaveTheSameChanges_theFileShouldExistInTheResultTree() throws IOException {
    writeToCache("/same_changes.txt", "some text data");
    AnyObjectId base = commit(null);

    byte[] bytes = "same changes".getBytes();
    clearCache();
    writeToCache("/same_changes.txt", bytes);
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/same_changes.txt", bytes);
    AnyObjectId theirs = commit(base);

    merger.merge(ours, theirs);
    AnyObjectId tree = merger.getResultTreeId();
    assertTrue(TreeUtils.exists("/same_changes.txt", tree, repo));
  }


}
