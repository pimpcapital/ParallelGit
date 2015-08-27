package com.beijunyi.parallelgit.utils;

import java.util.Iterator;
import javax.annotation.Nonnull;

import org.eclipse.jgit.dircache.*;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class CacheHelperTest {

  @Nonnull
  private static DirCache setupCache(String... files) {
    DirCache cache = DirCache.newInCore();
    DirCacheBuilder builder = cache.builder();
    for(String file : files)
      CacheHelper.addFile(builder, FileMode.REGULAR_FILE, file, ObjectId.zeroId());
    builder.finish();
    return cache;
  }

  private static void assertNextEntry(@Nonnull Iterator<VirtualDirCacheEntry> iterator, @Nonnull String path, boolean isRegularFile) {
    Assert.assertTrue(iterator.hasNext());
    VirtualDirCacheEntry entry = iterator.next();
    Assert.assertEquals(path, entry.getPath());
    Assert.assertEquals(isRegularFile, entry.isRegularFile());
  }

  @Test
  public void keepEverythingTest() {
    String file1 = "file1.txt";
    String file2 = "file2.txt";
    DirCache cache = setupCache(file1, file2);
    Assert.assertNotNull(cache.getEntry(file1));
    Assert.assertNotNull(cache.getEntry(file2));
  }

  @Test
  public void addFileTest() {
    DirCache cache = DirCache.newInCore();

    String file = "a/b/c.txt";
    AnyObjectId contentId1 = BlobHelper.getBlobId("a.b.c");
    CacheHelper.addFile(cache, file, contentId1);

    int entryCount = cache.getEntryCount();
    Assert.assertEquals(1, entryCount);

    int index = cache.findEntry(file);
    Assert.assertTrue(index >= 0);

    DirCacheEntry entry = cache.getEntry(index);
    Assert.assertNotNull(entry);
    Assert.assertEquals(contentId1, entry.getObjectId());
  }

  @Test
  public void addFilesWithDirCacheBuilderTest() {
    DirCache cache = DirCache.newInCore();

    DirCacheBuilder builder = cache.builder();
    String[] files = new String[] {"a/b/c1.txt",
                                    "a/c2.txt",
                                    "a/c3.txt",
                                    "a/b/c4.txt"};
    for(String file : files)
      CacheHelper.addFile(builder, FileMode.REGULAR_FILE, file, ObjectId.zeroId());builder.finish();

    int entryCount = cache.getEntryCount();
    Assert.assertEquals(4, entryCount);
  }

  @Test
  public void deleteFileTest() {
    DirCache cache = setupCache("a/b/c1.txt",
                                 "a/c2.txt",
                                 "a/c3.txt");

    CacheHelper.deleteFile(cache, "non_existent_file");
    Assert.assertEquals(3, cache.getEntryCount());

    CacheHelper.deleteFile(cache, "a/b/c1.txt");
    Assert.assertEquals(2, cache.getEntryCount());

    CacheHelper.deleteFile(cache, "a/c2.txt");
    Assert.assertEquals(1, cache.getEntryCount());
  }

  @Test
  public void deleteFilesWithDirCacheEditorTest() {
    DirCache cache = setupCache("a/b/c1.txt",
                                 "a/b/c2.txt",
                                 "a/c3.txt",
                                 "a/c4.txt",
                                 "a/c5.txt",
                                 "a/c6.txt");

    DirCacheEditor editor = cache.editor();
    CacheHelper.deleteFile(editor, "a/b/c1.txt");
    CacheHelper.deleteFile(editor, "a/c3.txt");
    CacheHelper.deleteFile(editor, "a/c4.txt");
    CacheHelper.deleteFile(editor, "a/c6.txt");
    editor.finish();

    Assert.assertEquals(2, cache.getEntryCount());
    Assert.assertNull(cache.getEntry("a/b/c1.txt"));
    Assert.assertNotNull(cache.getEntry("a/b/c2.txt"));
    Assert.assertNotNull(cache.getEntry("a/c5.txt"));
  }

  @Test
  public void deleteTreeTest() {
    DirCache cache = setupCache("a/b/c1.txt",
                                 "a/b/c2.txt",
                                 "a/c3.txt",
                                 "a/c4.txt",
                                 "a/c5.txt",
                                 "a/c6.txt");

    CacheHelper.deleteDirectory(cache, "a/b");

    Assert.assertEquals(4, cache.getEntryCount());
    Assert.assertNull(cache.getEntry("a/b/c1.txt"));
    Assert.assertNull(cache.getEntry("a/b/c2.txt"));
    Assert.assertNotNull(cache.getEntry("a/c3.txt"));
    Assert.assertNotNull(cache.getEntry("a/c4.txt"));
    Assert.assertNotNull(cache.getEntry("a/c5.txt"));
    Assert.assertNotNull(cache.getEntry("a/c6.txt"));
  }

  @Test
  public void deleteMultipleTreesTest() {
    DirCache cache = setupCache("a/b/c1.txt",
                                 "a/b/c2.txt",
                                 "a/d/c3.txt",
                                 "a/d/c4.txt",
                                 "a/c5.txt",
                                 "a/c6.txt");

    DirCacheEditor editor = cache.editor();
    CacheHelper.deleteDirectory(editor, "a/b");
    CacheHelper.deleteDirectory(editor, "a/d");
    editor.finish();

    Assert.assertEquals(2, cache.getEntryCount());
    Assert.assertNotNull(cache.getEntry("a/c5.txt"));
    Assert.assertNotNull(cache.getEntry("a/c6.txt"));
  }

  @Test
  public void isNonTrivialDirectoryTest() {
    DirCache cache = setupCache("a/b/c1.txt",
                                 "a/b/c2.txt",
                                 "a/d/c3.txt",
                                 "a/d/c4.txt",
                                 "a/c5.txt",
                                 "a/c6.txt");

    Assert.assertTrue(CacheHelper.isNonTrivialDirectory(cache, "a"));
    Assert.assertTrue(CacheHelper.isNonTrivialDirectory(cache, "a/b"));
    Assert.assertFalse(CacheHelper.isNonTrivialDirectory(cache, "a/c"));
    Assert.assertTrue(CacheHelper.isNonTrivialDirectory(cache, "a/d"));
  }

  @Test
  public void iterateDirectoryTest() {
    DirCache cache = setupCache("a/b/c1.txt",
                                 "a/b/c2.txt",
                                 "a/b/c/c3.txt",
                                 "a/c4.txt",
                                 "a/c5.txt",
                                 "a/d/c6.txt");

    Iterator<VirtualDirCacheEntry> iterator = CacheHelper.iterateDirectory(cache, "a");
    Assert.assertNotNull(iterator);
    assertNextEntry(iterator, "a/b", false);
    assertNextEntry(iterator, "a/c4.txt", true);
    assertNextEntry(iterator, "a/c5.txt", true);
    assertNextEntry(iterator, "a/d", false);
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  public void iterateNonExistentDirectoryTest() {
    DirCache cache = setupCache();
    Iterator<VirtualDirCacheEntry> iterator = CacheHelper.iterateDirectory(cache, "a");
    Assert.assertNull(iterator);
  }

  @Test
  public void iterateDirectoryThatHasPrefixNameChildrenTest() {
    DirCache cache = setupCache("prefix/file.txt",
                                 "prefix_plus_something/file.txt");
    Iterator<VirtualDirCacheEntry> iterator = CacheHelper.iterateDirectory(cache, "");
    Assert.assertNotNull(iterator);
    assertNextEntry(iterator, "prefix", false);
    assertNextEntry(iterator, "prefix_plus_something", false);
    Assert.assertFalse(iterator.hasNext());
  }

}
