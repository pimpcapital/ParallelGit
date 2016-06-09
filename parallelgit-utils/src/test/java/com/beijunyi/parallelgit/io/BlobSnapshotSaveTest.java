package com.beijunyi.parallelgit.io;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.BlobUtils;
import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BlobSnapshotSaveTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void captureBlobAndSave_shouldFindByteArrayFromBlobId() throws IOException {
    byte[] expected = someBytes();
    ObjectId blobId = BlobSnapshot.capture(expected).save(repo);

    assertArrayEquals(expected, BlobUtils.readBlob(blobId, repo).getData());
  }

  @Test
  public void loadBlobAndSave_theResultBlobIdShouldBeTheSame() throws IOException {
    ObjectId loaded = writeSomethingToCache();
    ObjectId saved = BlobSnapshot.load(loaded, repo).save(repo);

    assertEquals(loaded, saved);
  }

}
