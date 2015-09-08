package com.beijunyi.parallelgit.runtime;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.CacheUtils;
import com.beijunyi.parallelgit.utils.ObjectUtils;
import com.beijunyi.parallelgit.utils.RevTreeUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
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
    Assert.assertEquals(expectedFileBlob, CacheUtils.getBlobId(cache, "/expected_file.txt"));
  }

  @Test
  public void buildCacheWithBaseCommit_theResultCacheShouldHaveTheSameContentAsTheCommit2() throws IOException {
    AnyObjectId expectedFileBlob = writeToCache("/expected_file.txt");
    String commitIdStr = commitToMaster().getName();
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .baseCommit(commitIdStr)
                       .call();
    Assert.assertEquals(expectedFileBlob, CacheUtils.getBlobId(cache, "/expected_file.txt"));
  }

  @Test
  public void buildCacheWithBaseTree_theResultCacheShouldHaveTheSameContentAsTheTree1() throws IOException {
    AnyObjectId expectedFileBlob = writeToCache("/expected_file.txt");
    AnyObjectId treeId = RevTreeUtils.getRootTree(repo, commitToMaster());
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .baseTree(treeId)
                       .call();
    Assert.assertEquals(expectedFileBlob, CacheUtils.getBlobId(cache, "/expected_file.txt"));
  }

  @Test
  public void buildCacheWithBaseTree_theResultCacheShouldHaveTheSameContentAsTheTree2() throws IOException {
    AnyObjectId expectedFileBlob = writeToCache("/expected_file.txt");
    String treeIdStr = RevTreeUtils.getRootTree(repo, commitToMaster()).getName();
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .baseTree(treeIdStr)
                       .call();
    Assert.assertEquals(expectedFileBlob, CacheUtils.getBlobId(cache, "/expected_file.txt"));
  }

  @Test
  public void addFile_theResultCacheShouldHaveTheAddedFile() throws IOException {
    AnyObjectId blobId = ObjectUtils.calculateBlobId("some content");
    DirCache cache = ParallelCacheCommand.prepare()
                       .addFile("/expected_file.txt", blobId)
                       .call();
    Assert.assertNotNull(CacheUtils.getEntry(cache, "/expected_file.txt"));
  }

  @Test
  public void addFile_theResultFileShouldHaveTheSpecifiedBlobId() throws IOException {
    AnyObjectId blobId = ObjectUtils.calculateBlobId("some content");
    DirCache cache = ParallelCacheCommand.prepare()
                       .addFile("/expected_file.txt", blobId)
                       .call();
    Assert.assertEquals(blobId, CacheUtils.getBlobId(cache, "/expected_file.txt"));
  }

  @Test
  public void addFile_theResultFileShouldBeRegularFileByDefault() throws IOException {
    AnyObjectId blobId = ObjectUtils.calculateBlobId("some content");
    DirCache cache = ParallelCacheCommand.prepare()
                       .addFile("/expected_file.txt", blobId)
                       .call();
    Assert.assertEquals(FileMode.REGULAR_FILE, CacheUtils.getFileMode(cache, "/expected_file.txt"));
  }

  @Test
  public void addFileWithFileMode_theResultFileShouldHaveTheSpecifiedFileMode() throws IOException {
    AnyObjectId blobId = ObjectUtils.calculateBlobId("some content");
    FileMode mode = FileMode.EXECUTABLE_FILE;
    DirCache cache = ParallelCacheCommand.prepare()
                       .addFile("/expected_file.txt", blobId, mode)
                       .call();
    Assert.assertEquals(mode, CacheUtils.getFileMode(cache, "/expected_file.txt"));
  }

  @Test
  public void addDirectoryFromTree_theChildrenShouldExist() throws IOException {
    writeToCache("/child1.txt");
    writeToCache("/child2.txt");
    AnyObjectId treeWithTwoChildren = RevTreeUtils.getRootTree(repo, commitToMaster());
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .addDirectory("/dir", treeWithTwoChildren)
                       .call();
    Assert.assertNotNull(CacheUtils.getEntry(cache, "/dir/child1.txt"));
    Assert.assertNotNull(CacheUtils.getEntry(cache, "/dir/child2.txt"));
  }

  @Test
  public void addDirectoryFromTree_theChildrenShouldHaveTheInputBlobIds() throws IOException {
    AnyObjectId blob1 = writeToCache("/child1.txt");
    AnyObjectId blob2 = writeToCache("/child2.txt");
    AnyObjectId treeWithTwoChildren = RevTreeUtils.getRootTree(repo, commitToMaster());
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .addDirectory("/dir", treeWithTwoChildren)
                       .call();
    Assert.assertEquals(blob1, CacheUtils.getBlobId(cache, "/dir/child1.txt"));
    Assert.assertEquals(blob2, CacheUtils.getBlobId(cache, "/dir/child2.txt"));
  }

  @Test
  public void addDirectoryFromTreeIdString_theChildrenShouldExist() throws IOException {
    writeToCache("/child1.txt");
    writeToCache("/child2.txt");
    AnyObjectId treeWithTwoChildren = RevTreeUtils.getRootTree(repo, commitToMaster());
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .addDirectory("/dir", treeWithTwoChildren.getName())
                       .call();
    Assert.assertNotNull(CacheUtils.getEntry(cache, "/dir/child1.txt"));
    Assert.assertNotNull(CacheUtils.getEntry(cache, "/dir/child2.txt"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void addDirectoryWithoutRepository_shouldThrowIllegalArgumentException() throws IOException {
    writeToCache("/child1.txt");
    writeToCache("/child2.txt");
    AnyObjectId treeWithTwoChildren = RevTreeUtils.getRootTree(repo, commitToMaster());
    ParallelCacheCommand.prepare()
      .addDirectory(treeWithTwoChildren.getName(), "/dir")
      .call();
  }

  @Test
  public void deleteFile_theTargetFileShouldNotExistAfterDeletion() throws IOException {
    writeToCache("/file.txt");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .deleteFile("/file.txt")
                       .call();
    Assert.assertNull(cache.getEntry("/file.txt"));
  }

  @Test
  public void deleteFile_shouldHaveNoEffectOnOtherFile() throws IOException {
    writeToCache("/file.txt");
    writeToCache("/some_other_file.txt");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .deleteFile("/file.txt")
                       .call();
    Assert.assertNotNull(CacheUtils.getEntry(cache, "/some_other_file.txt"));
  }

  @Test
  public void deleteNonExistentFile_shouldHaveNoEffectOnTheCache() throws IOException {
    writeToCache("/some_file.txt");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .deleteFile("/non_existent_file.txt")
                       .call();
    Assert.assertEquals(1, cache.getEntryCount());
  }


  @Test
  public void deleteDirectory_childrenInTheTargetDirectoryShouldNotExistAfterDeletion() throws IOException {
    writeToCache("/dir/file1.txt");
    writeToCache("/dir/file2.txt");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .deleteDirectory("/dir")
                       .call();
    Assert.assertNull(CacheUtils.getEntry(cache, "/dir/file1.txt"));
    Assert.assertNull(CacheUtils.getEntry(cache, "/dir/file2.txt"));
  }

  @Test
  public void deleteDirectory_shouldHaveNoEffectOnFilesOutsideTheTargetDirectory() throws IOException {
    writeToCache("/a/b/1.txt");
    writeToCache("/a/b/2.txt");
    writeToCache("/a/3.txt");
    writeToCache("/4.txt");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .deleteDirectory("/a/b")
                       .call();
    Assert.assertNotNull(CacheUtils.getEntry(cache, "/a/3.txt"));
    Assert.assertNotNull(CacheUtils.getEntry(cache, "/4.txt"));
  }


  @Test
  public void updateFile_theTargetFileShouldHaveTheNewBlobIdAndFileMode() throws IOException {
    writeToCache("/file.txt", "old content");
    AnyObjectId commitId = commitToMaster();
    AnyObjectId newBlobId = ObjectUtils.calculateBlobId("new content");
    FileMode newFileMode = FileMode.EXECUTABLE_FILE;
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .updateFile("/file.txt", newBlobId, newFileMode)
                       .call();
    Assert.assertEquals(newBlobId, CacheUtils.getBlobId(cache, "/file.txt"));
    Assert.assertEquals(newFileMode, CacheUtils.getFileMode(cache, "/file.txt"));
  }

  @Test
  public void updateBlobIdOnly_theTargetFileShouldHaveTheNewBlobId() throws IOException {
    writeToCache("/file.txt", "old content");
    AnyObjectId commitId = commitToMaster();
    AnyObjectId newBlobId = ObjectUtils.calculateBlobId("new content");
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .updateFile("/file.txt", newBlobId)
                       .call();
    Assert.assertEquals(newBlobId, CacheUtils.getBlobId(cache, "/file.txt"));
  }

  @Test
  public void updateBlobIdOnly_theTargetFileShouldHaveTheOldFileMode() throws IOException {
    writeToCache("/file.txt", "some content".getBytes(), FileMode.SYMLINK);
    AnyObjectId commitId = commitToMaster();
    AnyObjectId newBlobId = ObjectUtils.calculateBlobId("new content");
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .updateFile("/file.txt", newBlobId)
                       .call();
    Assert.assertEquals(FileMode.SYMLINK, CacheUtils.getFileMode(cache, "/file.txt"));
  }

  @Test
  public void updateFileModeOnly_theTargetFileShouldHaveTheNewFileMode() throws IOException {
    writeToCache("/file.txt", "some content".getBytes());
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .updateFile("/file.txt", FileMode.EXECUTABLE_FILE)
                       .call();
    Assert.assertEquals(FileMode.EXECUTABLE_FILE, CacheUtils.getFileMode(cache, "/file.txt"));
  }

  @Test
  public void updateFileModeOnly_theTargetFileShouldHaveTheOldBlobId() throws IOException {
    AnyObjectId blobId = writeToCache("/file.txt", "some content".getBytes());
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .updateFile("/file.txt", FileMode.EXECUTABLE_FILE)
                       .call();
    Assert.assertEquals(blobId, CacheUtils.getBlobId(cache, "/file.txt"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateNonExistentFile_shouldThrowIllegalArgumentException() throws IOException {
    AnyObjectId commitId = commitToMaster();
    AnyObjectId blobId = ObjectUtils.calculateBlobId("new content");
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .updateFile("/non_existent_file.txt", blobId)
                       .call();
    Assert.assertNotNull(CacheUtils.getEntry(cache, "/non_existent_file.txt"));
  }

}
