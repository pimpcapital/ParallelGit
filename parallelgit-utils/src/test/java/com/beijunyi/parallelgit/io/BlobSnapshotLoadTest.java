package com.beijunyi.parallelgit.io;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class BlobSnapshotLoadTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void loadBlob_theResultShouldContainTheBlobData() throws IOException {
    byte[] expected = someBytes();
    ObjectId blob = writeToCache("/test_file.txt", expected);
    commit();
    BlobSnapshot snapshot = BlobSnapshot.load(blob, repo);

    assertArrayEquals(expected, snapshot.getData());
  }


}
