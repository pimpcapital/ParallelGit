package com.beijunyi.parallelgit.runtime;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.BlobHelper;
import com.beijunyi.parallelgit.utils.CacheHelper;
import com.beijunyi.parallelgit.utils.RevTreeHelper;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ParallelCacheCommandTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void buildCacheWithoutParameters_theResultCacheShouldBeEmpty() throws IOException {
    DirCache cache = ParallelCacheCommand.prepare().call();
    Assert.assertEquals(0, cache.getEntryCount());
  }

  @Test
  public void buildCacheWithBaseCommit_theResultCacheShouldHaveTheSameContentAsTheCommit1() throws IOException {
    AnyObjectId expectedFileBlob = writeToCache("/expected_file.txt");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .baseCommit(commitId)
                       .call();
    Assert.assertEquals(expectedFileBlob, CacheHelper.getBlobId(cache, "/expected_file.txt"));
  }

  @Test
  public void buildCacheWithBaseCommit_theResultCacheShouldHaveTheSameContentAsTheCommit2() throws IOException {
    AnyObjectId expectedFileBlob = writeToCache("/expected_file.txt");
    String commitIdStr = commitToMaster().getName();
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .baseCommit(commitIdStr)
                       .call();
    Assert.assertEquals(expectedFileBlob, CacheHelper.getBlobId(cache, "/expected_file.txt"));
  }

  @Test
  public void buildCacheWithBaseTree_theResultCacheShouldHaveTheSameContentAsTheTree1() throws IOException {
    AnyObjectId expectedFileBlob = writeToCache("/expected_file.txt");
    AnyObjectId treeId = RevTreeHelper.getRootTree(repo, commitToMaster());
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .baseTree(treeId)
                       .call();
    Assert.assertEquals(expectedFileBlob, CacheHelper.getBlobId(cache, "/expected_file.txt"));
  }

  @Test
  public void buildCacheWithBaseTree_theResultCacheShouldHaveTheSameContentAsTheTree2() throws IOException {
    AnyObjectId expectedFileBlob = writeToCache("/expected_file.txt");
    String treeIdStr = RevTreeHelper.getRootTree(repo, commitToMaster()).getName();
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .baseTree(treeIdStr)
                       .call();
    Assert.assertEquals(expectedFileBlob, CacheHelper.getBlobId(cache, "/expected_file.txt"));
  }

  @Test
  public void addFile_theResultCacheShouldHaveTheAddedFile() throws IOException {
    AnyObjectId blobId = BlobHelper.getBlobId(getClass().getName());
    DirCache cache = ParallelCacheCommand.prepare()
                       .addFile(blobId, "/expected_file.txt")
                       .call();
    Assert.assertNotNull(CacheHelper.getEntry(cache, "/expected_file.txt"));
  }

  @Test
  public void addFile_theResultFileShouldHaveTheSpecifiedBlobId() throws IOException {
    AnyObjectId blobId = BlobHelper.getBlobId(getClass().getName());
    DirCache cache = ParallelCacheCommand.prepare()
                       .addFile(blobId, "/expected_file.txt")
                       .call();
    Assert.assertEquals(blobId, CacheHelper.getBlobId(cache, "/expected_file.txt"));
  }

  @Test
  public void addFile_theResultFileShouldBeRegularFileByDefault() throws IOException {
    AnyObjectId blobId = BlobHelper.getBlobId(getClass().getName());
    DirCache cache = ParallelCacheCommand.prepare()
                       .addFile(blobId, "/expected_file.txt")
                       .call();
    Assert.assertEquals(FileMode.REGULAR_FILE, CacheHelper.getFileMode(cache, "/expected_file.txt"));
  }

  @Test
  public void addFileWithFileMode_theResultFileShouldHaveTheSpecifiedFileMode() throws IOException {
    AnyObjectId blobId = BlobHelper.getBlobId(getClass().getName());
    FileMode mode = FileMode.EXECUTABLE_FILE;
    DirCache cache = ParallelCacheCommand.prepare()
                       .addFile(blobId, mode, "/expected_file.txt")
                       .call();
    Assert.assertEquals(mode, CacheHelper.getFileMode(cache, "/expected_file.txt"));
  }


  @Test
  public void addTreeTest() throws IOException {
    AnyObjectId blob1 = writeToCache("/expected_file1.txt");
    AnyObjectId blob2 = writeToCache("/expected_file2.txt");
    AnyObjectId treeId = RevTreeHelper.getRootTree(repo, commitToMaster());
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .addDirectory(treeId, "base")
                       .call();
    Assert.assertEquals(blob1, cache.getEntry("base/expected_file1.txt").getObjectId());
    Assert.assertEquals(blob2, cache.getEntry("base/expected_file2.txt").getObjectId());
  }

  @Test
  public void addTreeFromTreeIdStringTest() throws IOException {
    AnyObjectId blob1 = writeToCache("1.txt");
    AnyObjectId blob2 = writeToCache("2.txt");
    String treeIdStr = RevTreeHelper.getRootTree(repo, commitToMaster()).getName();
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .addDirectory(treeIdStr, "base")
                       .call();
    Assert.assertEquals(blob1, cache.getEntry("base/1.txt").getObjectId());
    Assert.assertEquals(blob2, cache.getEntry("base/2.txt").getObjectId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void addTreeWithoutRepositoryTest() throws IOException {
    ParallelCacheCommand
      .prepare()
      .addDirectory(ObjectId.zeroId(), "somepath")
      .call();
  }

  @Test
  public void deleteBlobTest() throws IOException {
    writeToCache("1.txt");
    writeToCache("2.txt");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .deleteFile("1.txt")
                       .call();
    Assert.assertNull(cache.getEntry("1.txt"));
    Assert.assertNotNull(cache.getEntry("2.txt"));
  }

  @Test
  public void deleteTreeTest() throws IOException {
    writeToCache("a/b/1.txt");
    writeToCache("a/b/2.txt");
    writeToCache("a/3.txt");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .deleteDirectory("a/b")
                       .call();
    Assert.assertNull(cache.getEntry("a/b/1.txt"));
    Assert.assertNull(cache.getEntry("a/b/2.txt"));
    Assert.assertNotNull(cache.getEntry("a/3.txt"));
  }

  @Test
  public void updateBlobTest() throws IOException {
    writeToCache("1.txt", "some content");
    AnyObjectId commitId = commitToMaster();
    AnyObjectId newBlobId = BlobHelper.getBlobId("some other content");
    FileMode newFileMode = FileMode.EXECUTABLE_FILE;
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .updateFile(newBlobId, newFileMode, "1.txt")
                       .call();
    DirCacheEntry entry = cache.getEntry("1.txt");
    Assert.assertEquals(newBlobId, entry.getObjectId());
    Assert.assertEquals(newFileMode, entry.getFileMode());
  }

  @Test
  public void updateBlobIdTest() throws IOException {
    writeToCache("1.txt", "some content");
    AnyObjectId commitId = commitToMaster();
    AnyObjectId newBlobId = BlobHelper.getBlobId("some other content");
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .updateFile(newBlobId, "1.txt")
                       .call();
    DirCacheEntry entry = cache.getEntry("1.txt");
    Assert.assertEquals(newBlobId, entry.getObjectId());
  }

  @Test
  public void updateBlobFileModeTest() throws IOException {
    writeToCache("1.txt", "some content");
    AnyObjectId commitId = commitToMaster();
    FileMode newFileMode = FileMode.EXECUTABLE_FILE;
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .updateFile(newFileMode, "1.txt")
                       .call();
    DirCacheEntry entry = cache.getEntry("1.txt");
    Assert.assertEquals(newFileMode, entry.getFileMode());
  }

}
