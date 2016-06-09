package com.beijunyi.parallelgit.io;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.io.GitFileEntry.*;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;
import static org.junit.Assert.*;

public class TreeSnapshotGetChildTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void getChild_shouldReturnTheChildEntry() throws IOException {
    ObjectId id = writeToCache("/test_file.txt", someBytes(), REGULAR_FILE);
    ObjectId tree = commit().getTree();
    TreeSnapshot snapshot = TreeSnapshot.load(tree, repo);
    assertEquals(newEntry(id, REGULAR_FILE), snapshot.getChild("test_file.txt"));
  }

  @Test
  public void getChildWhenSpecifiedChildDoesNotExist_shouldReturnMissingEntry() throws IOException {
    writeSomethingToCache();
    ObjectId tree = commit().getTree();
    TreeSnapshot snapshot = TreeSnapshot.load(tree, repo);
    assertEquals(missingEntry(), snapshot.getChild("non_existent_file.txt"));
  }

  @Test
  public void testHasChildWhenChildExists_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    ObjectId tree = commit().getTree();
    TreeSnapshot snapshot = TreeSnapshot.load(tree, repo);
    assertTrue(snapshot.hasChild("test_file.txt"));
  }

  @Test
  public void testHasChildWhenChildDoesNotExist_shouldReturnFalse() throws IOException {
    writeSomethingToCache();
    ObjectId tree = commit().getTree();
    TreeSnapshot snapshot = TreeSnapshot.load(tree, repo);
    assertFalse(snapshot.hasChild("non_existent_file.txt"));
  }

}
