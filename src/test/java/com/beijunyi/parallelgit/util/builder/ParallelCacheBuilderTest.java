package com.beijunyi.parallelgit.util.builder;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.util.BlobHelper;
import com.beijunyi.parallelgit.util.RevTreeHelper;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class ParallelCacheBuilderTest extends AbstractParallelGitTest {

  @Test
  public void buildEmptyCacheTest() throws IOException {
    DirCache cache = ParallelCacheBuilder.prepare().build();
    Assert.assertEquals(0, cache.getEntryCount());
  }

  @Test
  public void buildCacheFromRevisionTest() throws IOException {
    initRepository();
    ObjectId blob1 = writeFile("1.txt");
    ObjectId blob2 = writeFile("2.txt");
    ObjectId revisionId = commitToMaster();
    DirCache cache = ParallelCacheBuilder.prepare(repo)
                       .loadRevision(revisionId)
                       .build();
    Assert.assertEquals(2, cache.getEntryCount());
    Assert.assertEquals(blob1, cache.getEntry("1.txt").getObjectId());
    Assert.assertEquals(blob2, cache.getEntry("2.txt").getObjectId());
  }

  @Test
  public void buildCacheFromRevisionStringTest() throws IOException {
    initRepository();
    ObjectId blob1 = writeFile("1.txt");
    ObjectId blob2 = writeFile("2.txt");
    String revisionIdStr = commitToMaster().getName();
    DirCache cache = ParallelCacheBuilder.prepare(repo)
                       .loadRevision(revisionIdStr)
                       .build();
    Assert.assertEquals(2, cache.getEntryCount());
    Assert.assertEquals(blob1, cache.getEntry("1.txt").getObjectId());
    Assert.assertEquals(blob2, cache.getEntry("2.txt").getObjectId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void tryLoadingRevisionToInitializedCacheTest() throws IOException {
    initRepository();
    ObjectId blob1 = writeFile("1.txt");
    ObjectId revisionId = commitToMaster();
    ParallelCacheBuilder.prepare(repo).addBlob(blob1, "1.txt").loadRevision(revisionId).build();
  }

  @Test
  public void addBlobTest() throws IOException {
    ObjectId blobId = BlobHelper.getBlobId(getClass().getName());
    String path = "test.txt";
    DirCache cache = ParallelCacheBuilder.prepare()
                       .addBlob(blobId, path)
                       .build();
    Assert.assertEquals(1, cache.getEntryCount());
    DirCacheEntry entry = cache.getEntry(path);
    Assert.assertEquals(blobId, entry.getObjectId());
    Assert.assertEquals(FileMode.REGULAR_FILE, entry.getFileMode());
  }

  @Test
  public void addBlobWithSpecifiedFileModeTest() throws IOException {
    ObjectId blobId = BlobHelper.getBlobId(getClass().getName());
    String path = "test.txt";
    FileMode mode = FileMode.EXECUTABLE_FILE;
    DirCache cache = ParallelCacheBuilder.prepare()
                       .addBlob(blobId, mode, path)
                       .build();
    Assert.assertEquals(1, cache.getEntryCount());
    DirCacheEntry entry = cache.getEntry(path);
    Assert.assertEquals(blobId, entry.getObjectId());
    Assert.assertEquals(mode, entry.getFileMode());
  }


  @Test
  public void addTreeTest() throws IOException {
    initRepository();
    ObjectId blob1 = writeFile("1.txt");
    ObjectId blob2 = writeFile("2.txt");
    ObjectId treeId = RevTreeHelper.getRootTree(repo, commitToMaster());
    DirCache cache = ParallelCacheBuilder.prepare(repo)
                       .addTree(treeId, "base")
                       .build();
    Assert.assertEquals(2, cache.getEntryCount());
    Assert.assertEquals(blob1, cache.getEntry("base/1.txt").getObjectId());
    Assert.assertEquals(blob2, cache.getEntry("base/2.txt").getObjectId());
  }

  @Test
  public void addTreeFromTreeIdStringTest() throws IOException {
    initRepository();
    ObjectId blob1 = writeFile("1.txt");
    ObjectId blob2 = writeFile("2.txt");
    String treeIdStr = RevTreeHelper.getRootTree(repo, commitToMaster()).getName();
    DirCache cache = ParallelCacheBuilder.prepare(repo)
                       .addTree(treeIdStr, "base")
                       .build();
    Assert.assertEquals(2, cache.getEntryCount());
    Assert.assertEquals(blob1, cache.getEntry("base/1.txt").getObjectId());
    Assert.assertEquals(blob2, cache.getEntry("base/2.txt").getObjectId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void addTreeWithoutRepositoryTest() throws IOException {
    ParallelCacheBuilder.prepare().addTree(ObjectId.zeroId(), "somepath").build();
  }

  @Test
  public void deleteBlobTest() throws IOException {
    initRepository();
    writeFile("1.txt");
    writeFile("2.txt");
    ObjectId revisionId = commitToMaster();
    DirCache cache = ParallelCacheBuilder
                       .prepare(repo)
                       .loadRevision(revisionId)
                       .deleteBlob("1.txt")
                       .build();
    Assert.assertEquals(1, cache.getEntryCount());
    Assert.assertNull(cache.getEntry("1.txt"));
    Assert.assertNotNull(cache.getEntry("2.txt"));
  }

  @Test
  public void deleteTreeTest() throws IOException {
    initRepository();
    writeFile("a/b/1.txt");
    writeFile("a/b/2.txt");
    writeFile("a/3.txt");
    ObjectId revisionId = commitToMaster();
    DirCache cache = ParallelCacheBuilder
                       .prepare(repo)
                       .loadRevision(revisionId)
                       .deleteTree("a/b")
                       .build();
    Assert.assertEquals(1, cache.getEntryCount());
    Assert.assertNull(cache.getEntry("a/b/1.txt"));
    Assert.assertNull(cache.getEntry("a/b/2.txt"));
    Assert.assertNotNull(cache.getEntry("a/3.txt"));
  }

  @Test
  public void updateBlobTest() throws IOException {
    initRepository();
    writeFile("1.txt", "some content");
    ObjectId revisionId = commitToMaster();
    ObjectId newBlobId = BlobHelper.getBlobId("some other content");
    FileMode newFileMode = FileMode.EXECUTABLE_FILE;
    DirCache cache = ParallelCacheBuilder
                       .prepare(repo)
                       .loadRevision(revisionId)
                       .updateBlob(newBlobId, newFileMode, "1.txt")
                       .build();
    DirCacheEntry entry = cache.getEntry("1.txt");
    Assert.assertEquals(newBlobId, entry.getObjectId());
    Assert.assertEquals(newFileMode, entry.getFileMode());
  }

}
