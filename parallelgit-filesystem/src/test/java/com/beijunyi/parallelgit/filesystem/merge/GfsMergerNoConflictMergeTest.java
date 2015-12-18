package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@Ignore
public class GfsMergerNoConflictMergeTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void whenMergeFinishesSuccessfully_shouldReturnTrue() throws IOException {
    AnyObjectId base = commit(null);

    clearCache();
    writeToCache("/ours.txt");
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/theirs.txt");
    AnyObjectId theirs = commit(base);

    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      assertTrue(merger.merge(ours, theirs));
    }
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

    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      assertNotNull(merger.getResultTreeId());
    }
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

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
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

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
    assertTrue(TreeUtils.exists("/dir/ours.txt", tree, repo));
    assertTrue(TreeUtils.exists("/dir/theirs.txt", tree, repo));
  }

  @Test
  public void whenOursAndTheirsHaveTheSameInsertion_theFileShouldExistInTheResultTree() throws IOException {
    AnyObjectId base = commit(null);

    byte[] bytes = "same insertion".getBytes();
    clearCache();
    writeToCache("/same_insertion.txt", bytes);
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/same_insertion.txt", bytes);
    AnyObjectId theirs = commit(base);

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
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

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
    assertTrue(TreeUtils.exists("/same_insertion.txt", tree, repo));
  }

  @Test
  public void whenOursAndTheirsDeleteTheSameFile_theFileShouldNotExistInTheResultTree() throws IOException {
    writeToCache("/same_deletion.txt", "same deletion".getBytes());
    AnyObjectId base = commit(null);

    clearCache();
    writeSomeFileToCache();
    AnyObjectId ours = commit(base);

    clearCache();
    writeSomeFileToCache();
    AnyObjectId theirs = commit(base);

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
    assertFalse(TreeUtils.exists("/same_deletion.txt", tree, repo));
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

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
    assertTrue(TreeUtils.exists("/dir", tree, repo));
  }

  @Test
  public void whenOursAndTheirsDeleteTheSameDirectory_theDirectoryShouldNotExistInTheResultTree() throws IOException {
    writeToCache("/dir/same_deletion.txt", "same deletion".getBytes());
    AnyObjectId base = commit(null);

    clearCache();
    writeSomeFileToCache();
    AnyObjectId ours = commit(base);

    clearCache();
    writeSomeFileToCache();
    AnyObjectId theirs = commit(base);

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
    assertFalse(TreeUtils.exists("/dir", tree, repo));
  }

  @Test
  public void whenOursAndTheirsHaveTheSameChanges_theFileInTheResultTreeShouldHaveTheNewContent() throws IOException {
    writeToCache("/same_changes.txt", "some text data");
    AnyObjectId base = commit(null);

    byte[] expected = "same changes".getBytes();
    clearCache();
    writeToCache("/same_changes.txt", expected);
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/same_changes.txt", expected);
    AnyObjectId theirs = commit(base);

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
    assertArrayEquals(expected, TreeUtils.readFile("/same_changes.txt", tree, repo).getBytes());
  }

  @Test
  public void whenOursAndTheirsBothChangeDirectoryIntoFile_theNewFileShouldExistInTheResultTree() throws IOException {
    writeToCache("/target/some_file.txt", "some text data");
    AnyObjectId base = commit(null);

    byte[] expected = "same file content".getBytes();
    clearCache();
    writeToCache("/target", expected);
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/target", expected);
    AnyObjectId theirs = commit(base);

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
    assertArrayEquals(expected, TreeUtils.readFile("/target", tree, repo).getBytes());
  }

  @Test
  public void whenOursAndTheirsBothChangeFileIntoDirectory_theNewDirectoryShouldExistInTheResultTree() throws IOException {
    writeToCache("/target", "some text data");
    AnyObjectId base = commit(null);

    byte[] expected = "same file content".getBytes();
    clearCache();
    writeToCache("/target/test_file.txt", expected);
    AnyObjectId ours = commit(base);

    clearCache();
    writeToCache("/target/test_file.txt", expected);
    AnyObjectId theirs = commit(base);

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
    assertArrayEquals(expected, TreeUtils.readFile("/target/test_file.txt", tree, repo).getBytes());
  }

  @Test
  public void whenContentFromOursAndTheirsCanBeAutoMerged_() throws IOException {
    writeToCache("/test_file.txt", "a\nb\nc\nd\ne");
    RevCommit base = commit(null);

    clearCache();
    writeToCache("/test_file.txt", "a\nB\nc\nd\ne");
    RevCommit ours = commit(base);

    clearCache();
    writeToCache("/test_file.txt", "a\nB\nc\nD\nd\ne");
    RevCommit theirs = commit(base);

    AnyObjectId tree;
    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
      GfsMerger merger = new GfsMerger(gfs);
      merger.merge(ours, theirs);
      tree = merger.getResultTreeId();
    }
    assertArrayEquals("".getBytes(), TreeUtils.readFile("/test_file.txt", tree, repo).getBytes());
  }

}
