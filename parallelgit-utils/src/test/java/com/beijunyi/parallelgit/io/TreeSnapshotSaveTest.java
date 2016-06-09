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
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;
import static org.junit.Assert.assertEquals;

public class TreeSnapshotSaveTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void createSnapshotAndSave_shouldFindChildrenEntriesFromTreeId() throws IOException {
    SortedMap<String, GitFileEntry> children = new TreeMap<>();
    GitFileEntry entry1 = newEntry(someObjectId(), REGULAR_FILE);
    children.put("file1.txt", entry1);
    GitFileEntry entry2 = newEntry(someObjectId(), REGULAR_FILE);
    children.put("file2.txt", entry2);
    ObjectId treeId = TreeSnapshot.capture(children).save(repo);

    assertEquals(entry1, newEntry("file1.txt", treeId, repo));
    assertEquals(entry2, newEntry("file2.txt", treeId, repo));
  }

  @Test
  public void loadSnapshotAndSave_theResultTreeIdShouldBeTheSame() throws IOException {
    writeSomethingToCache();
    ObjectId loaded = commit().getTree();
    ObjectId saved = TreeSnapshot.load(loaded, repo).save(repo);

    assertEquals(loaded, saved);
  }

}
