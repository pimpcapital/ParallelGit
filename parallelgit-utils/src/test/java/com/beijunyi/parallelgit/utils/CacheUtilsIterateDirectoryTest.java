package com.beijunyi.parallelgit.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchCacheDirectoryException;
import com.beijunyi.parallelgit.utils.exceptions.NoSuchCacheEntryException;
import com.beijunyi.parallelgit.utils.io.CacheNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CacheUtilsIterateDirectoryTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void testHasNextWhenThereAreRemainingNodes_shouldReturnTrue() throws IOException {
    writeMultipleToCache
      (
        "/file1.txt",
        "/file2.txt",
        "/file3.txt"
      );
    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/", cache);
    iterator.next();
    Assert.assertTrue(iterator.hasNext());
  }

  @Test
  public void testHasNextWhenThereIsNoRemainingNode_shouldReturnFalse() throws IOException {
    writeMultipleToCache
      (
        "/file1.txt",
        "/file2.txt",
        "/file3.txt"
      );
    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/", cache);
    iterator.next();
    iterator.next();
    iterator.next();
    Assert.assertFalse(iterator.hasNext());
  }

  @Test(expected = NoSuchElementException.class)
  public void getHasNextWhenThereIsNoRemainingNode_shouldThrowNoSuchElementException() throws IOException {
    writeMultipleToCache
      (
        "/file1.txt",
        "/file2.txt",
        "/file3.txt"
      );
    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/", cache);
    iterator.next();
    iterator.next();
    iterator.next();
    iterator.next();
  }

  @Test
  public void iterateDirectory_shouldReturnTheDirectChildrenNodesInAlphabeticalOrder() throws IOException {
    writeMultipleToCache
      (
        "/a/aa/aaa/aaaa.txt",
        "/a/aa/aab.txt",
        "/a/aa/aac.txt",
        "/a/ab.txt",
        "/a/ac.txt",
        "/a/ad/ada.txt",
        "/b/ba.txt"
      );

    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/a", cache);
    Assert.assertEquals("/a/aa", iterator.next().getPath());
    Assert.assertEquals("/a/ab.txt", iterator.next().getPath());
    Assert.assertEquals("/a/ac.txt", iterator.next().getPath());
    Assert.assertEquals("/a/ad", iterator.next().getPath());
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  public void iterateDirectoryRecursively_shouldReturnAllChildrenNodesInAlphabeticalOrder() throws IOException {
    writeMultipleToCache
      (
        "/a/aa/aaa/aaaa.txt",
        "/a/aa/aab.txt",
        "/a/aa/aac.txt",
        "/a/ab.txt",
        "/a/ac.txt",
        "/a/ad/ada.txt",
        "/b/ba.txt"
      );

    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/a", true, cache);
    Assert.assertEquals("/a/aa/aaa/aaaa.txt", iterator.next().getPath());
    Assert.assertEquals("/a/aa/aab.txt", iterator.next().getPath());
    Assert.assertEquals("/a/aa/aac.txt", iterator.next().getPath());
    Assert.assertEquals("/a/ab.txt", iterator.next().getPath());
    Assert.assertEquals("/a/ac.txt", iterator.next().getPath());
    Assert.assertEquals("/a/ad/ada.txt", iterator.next().getPath());
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  public void testIsFileWhenCacheNodeIsFile_shouldReturnTrue() throws IOException {
    writeToCache("/file.txt");
    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/", cache);
    Assert.assertTrue(iterator.next().isFile());
  }

  @Test
  public void testIsFileWhenCacheNodeIsDirectory_shouldReturnTrue() throws IOException {
    writeToCache("/dir/file.txt");
    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/", cache);
    Assert.assertFalse(iterator.next().isFile());
  }

  @Test
  public void testIsDirectoryWhenCacheNodeIsFile_shouldReturnFalse() throws IOException {
    writeToCache("/file.txt");
    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/", cache);
    Assert.assertFalse(iterator.next().isDirectory());
  }

  @Test
  public void testIsDirectoryWhenCacheNodeIsDirectory_shouldReturnTrue() throws IOException {
    writeToCache("/dir/file.txt");
    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/", cache);
    Assert.assertTrue(iterator.next().isDirectory());
  }

  @Test
  public void getEntryWhenCacheNodeIsFile_shouldReturnTheFileEntry() throws IOException {
    writeToCache("/file.txt");
    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/", cache);
    Assert.assertEquals("file.txt", iterator.next().getEntry().getPathString());
  }

  @Test(expected = NoSuchCacheEntryException.class)
  public void getEntryWhenCacheNodeIsDirectory_shouldThrowNoSuchCacheEntryException() throws IOException {
    writeToCache("/dir/file.txt");
    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/", cache);
    iterator.next().getEntry();
  }

  @Test(expected = NoSuchCacheDirectoryException.class)
  public void iterateDirectoryWhenDirectoryDoesNotExist_shouldThrowNoSuchCacheDirectoryException() throws IOException {
    CacheUtils.iterateDirectory("/a", cache);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void removeNode_shouldThrowUnsupportedOperationException() throws IOException {
    writeMultipleToCache
      (
        "/file1.txt",
        "/file2.txt",
        "/file3.txt"
      );
    Iterator<CacheNode> iterator = CacheUtils.iterateDirectory("/", cache);
    iterator.remove();
  }

}
