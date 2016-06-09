package com.beijunyi.parallelgit.io;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.io.GitFileEntry.missingEntry;
import static org.eclipse.jgit.lib.FileMode.*;
import static org.junit.Assert.*;

public class TreeSnapshotLoadTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void loadTree_theResultShouldContainTheChildrenEntries() throws IOException {
    writeToCache("/file1.txt");
    writeToCache("/file2.txt");
    ObjectId tree = commit().getTree();
    TreeSnapshot snapshot = TreeSnapshot.load(tree, repo);
    assertNotEquals(missingEntry(), snapshot.getChild("file1.txt"));
    assertNotEquals(missingEntry(), snapshot.getChild("file2.txt"));
    assertFalse(snapshot.hasChild("non_existent_file.txt"));
  }

  @Test
  public void loadTree_theChildrenEntriesShouldHaveTheCorrectIds() throws IOException {
    ObjectId id1 = writeToCache("/file1.txt");
    ObjectId id2 = writeToCache("/file2.txt");
    ObjectId tree = commit().getTree();
    TreeSnapshot snapshot = TreeSnapshot.load(tree, repo);
    assertEquals(id1, snapshot.getChild("file1.txt").getId());
    assertEquals(id2, snapshot.getChild("file2.txt").getId());
  }

  @Test
  public void loadTree_theChildrenEntriesShouldHaveTheCorrectFileModes() throws IOException {
    writeToCache("/file1.txt", someBytes(), EXECUTABLE_FILE);
    writeToCache("/file2.txt", someBytes(), REGULAR_FILE);
    ObjectId tree = commit().getTree();
    TreeSnapshot snapshot = TreeSnapshot.load(tree, repo);
    assertEquals(EXECUTABLE_FILE, snapshot.getChild("file1.txt").getMode());
    assertEquals(REGULAR_FILE, snapshot.getChild("file2.txt").getMode());
  }

  @Test
  public void loadTreeWithSubtree_theSubtreeEntryFileModeShouldBeTree() throws IOException {
    writeToCache("/dir/some_file.txt");
    ObjectId tree = commit().getTree();
    TreeSnapshot snapshot = TreeSnapshot.load(tree, repo);
    assertEquals(TREE, snapshot.getChild("dir").getMode());
  }




}
