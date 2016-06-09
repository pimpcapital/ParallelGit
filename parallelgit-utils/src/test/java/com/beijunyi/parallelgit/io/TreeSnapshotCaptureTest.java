package com.beijunyi.parallelgit.io;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.io.GitFileEntry.newEntry;
import static org.eclipse.jgit.lib.FileMode.*;
import static org.junit.Assert.*;

public class TreeSnapshotCaptureTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void captureSnapshot_theResultShouldContainTheChildrenEntries() throws IOException {
    SortedMap<String, GitFileEntry> children = new TreeMap<>();
    children.put("file1.txt", newEntry(someObjectId(), REGULAR_FILE));
    children.put("file2.txt", newEntry(someObjectId(), REGULAR_FILE));
    TreeSnapshot snapshot = TreeSnapshot.capture(children);
    assertTrue(snapshot.hasChild("file1.txt"));
    assertTrue(snapshot.hasChild("file2.txt"));
    assertFalse(snapshot.hasChild("non_existent_file.txt"));
  }

  @Test
  public void captureSnapshot_theChildrenEntriesShouldHaveTheCorrectIds() throws IOException {
    ObjectId id1 = someObjectId();
    ObjectId id2 = someObjectId();
    SortedMap<String, GitFileEntry> children = new TreeMap<>();
    children.put("file1.txt", newEntry(id1, REGULAR_FILE));
    children.put("file2.txt", newEntry(id2, REGULAR_FILE));
    TreeSnapshot snapshot = TreeSnapshot.capture(children);
    assertEquals(id1, snapshot.getChild("file1.txt").getId());
    assertEquals(id2, snapshot.getChild("file2.txt").getId());
  }

  @Test
  public void captureSnapshot_theChildrenEntriesShouldHaveTheCorrectFileModes() throws IOException {
    SortedMap<String, GitFileEntry> children = new TreeMap<>();
    children.put("file1.txt", newEntry(someObjectId(), REGULAR_FILE));
    children.put("file2.txt", newEntry(someObjectId(), EXECUTABLE_FILE));
    TreeSnapshot snapshot = TreeSnapshot.capture(children);
    assertEquals(REGULAR_FILE, snapshot.getChild("file1.txt").getMode());
    assertEquals(EXECUTABLE_FILE, snapshot.getChild("file2.txt").getMode());
  }


}
