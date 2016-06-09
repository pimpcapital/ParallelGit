package com.beijunyi.parallelgit.io;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class BlobSnapshotCaptureTest extends AbstractParallelGitTest {

  @Test
  public void captureBlob_shouldFind() throws IOException {
    byte[] expected = someBytes();
    BlobSnapshot snapshot = BlobSnapshot.capture(expected);

    assertArrayEquals(expected, snapshot.getData());
  }


}
