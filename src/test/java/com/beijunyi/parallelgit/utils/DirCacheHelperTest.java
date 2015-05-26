package com.beijunyi.parallelgit.utils;

import java.util.*;

import org.eclipse.jgit.dircache.*;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class DirCacheHelperTest {

  @Test
  public void keepEverythingTest() {
    DirCache cache = DirCache.newInCore();
    DirCacheBuilder builder = cache.builder();
    String file1 = "file1.txt";
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, file1, ObjectId.zeroId());
    String file2 = "file2.txt";
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, file2, ObjectId.zeroId());
    String file4 = "file4.txt";
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, file4, ObjectId.zeroId());
    builder.finish();
    builder = DirCacheHelper.keepEverything(cache);
    String file3 = "file3.txt";
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, file3, ObjectId.zeroId());
    builder.finish();
    Assert.assertNotNull(cache.getEntry(file1));
    Assert.assertNotNull(cache.getEntry(file2));
    Assert.assertNotNull(cache.getEntry(file3));
    Assert.assertNotNull(cache.getEntry(file4));
  }

  @Test
  public void addFileTest() {
    DirCache cache = DirCache.newInCore();

    ObjectId contentId1 = BlobHelper.calculateBlobId("a.b.c");
    ObjectId contentId2 = BlobHelper.calculateBlobId("a.b.d");
    String file1 = "a/b/c.txt";
    DirCacheHelper.addFile(cache, file1, contentId1);
    String file2 = "a/b/d.txt";
    DirCacheHelper.addFile(cache, file2, contentId2);
    int entryCount = cache.getEntryCount();
    Assert.assertEquals(2, entryCount);

    int index1 = cache.findEntry(file1);
    Assert.assertTrue(index1 >= 0);
    int index2 = cache.findEntry(file2);
    Assert.assertTrue(index2 >= 0);

    DirCacheEntry entry1 = cache.getEntry(index1);
    Assert.assertNotNull(entry1);
    DirCacheEntry entry2 = cache.getEntry(index2);
    Assert.assertNotNull(entry2);

    ObjectId pointedObjectId1 = entry1.getObjectId();
    Assert.assertEquals(contentId1, pointedObjectId1);
    ObjectId pointedObjectId2 = entry2.getObjectId();
    Assert.assertEquals(contentId2, pointedObjectId2);
  }

  @Test
  public void addFilesWithDirCacheBuilderTest() {
    DirCache cache = DirCache.newInCore();

    DirCacheBuilder builder = cache.builder();

    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/b/c1.txt", ObjectId.zeroId());
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/c2.txt", ObjectId.zeroId());
    DirCacheHelper.addFile(builder, FileMode.EXECUTABLE_FILE, "c3.txt", ObjectId.zeroId());
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/b/c4.txt", ObjectId.zeroId());

    builder.finish();

    int entryCount = cache.getEntryCount();
    Assert.assertEquals(4, entryCount);
  }

  @Test
  public void deleteFileTest() {
    DirCache cache = DirCache.newInCore();

    DirCacheBuilder builder = cache.builder();
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/b/c1.txt", ObjectId.zeroId());
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/c2.txt", ObjectId.zeroId());
    DirCacheHelper.addFile(builder, FileMode.EXECUTABLE_FILE, "a/c3.txt", ObjectId.zeroId());
    builder.finish();

    DirCacheHelper.deleteFile(cache, "non_existent_file");
    Assert.assertEquals(3, cache.getEntryCount());

    DirCacheHelper.deleteFile(cache, "a/b/c1.txt");
    Assert.assertEquals(2, cache.getEntryCount());

    DirCacheHelper.deleteFile(cache, "a/c2.txt");
    Assert.assertEquals(1, cache.getEntryCount());
  }

  @Test
  public void deleteFilesWithDirCacheEditorTest() {
    DirCache cache = DirCache.newInCore();

    DirCacheBuilder builder = cache.builder();
    String[] files = new String[] {"a/b/c1.txt", "a/b/c2.txt", "a/c3.txt", "a/c4.txt", "a/c5.txt", "a/c6.txt"};
    for(String file : files)
      DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, file, ObjectId.zeroId());
    builder.finish();

    DirCacheEditor editor = cache.editor();
    DirCacheHelper.deleteFile(editor, "a/b/c1.txt");
    DirCacheHelper.deleteFile(editor, "a/c3.txt");
    DirCacheHelper.deleteFile(editor, "a/c4.txt");
    DirCacheHelper.deleteFile(editor, "a/c6.txt");
    editor.finish();

    Assert.assertEquals(2, cache.getEntryCount());
    Assert.assertNull(cache.getEntry("a/b/c1.txt"));
    Assert.assertNotNull(cache.getEntry("a/b/c2.txt"));
    Assert.assertNotNull(cache.getEntry("a/c5.txt"));
  }

  @Test
  public void deleteTreeTest() {
    DirCache cache = DirCache.newInCore();

    DirCacheBuilder builder = cache.builder();
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/b/c1.txt", ObjectId.zeroId());
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/b/c2.txt", ObjectId.zeroId());
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/c3.txt", ObjectId.zeroId());
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/c4.txt", ObjectId.zeroId());
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/c5.txt", ObjectId.zeroId());
    DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, "a/c6.txt", ObjectId.zeroId());
    builder.finish();

    DirCacheHelper.deleteDirectory(cache, "a/b");

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
    DirCache cache = DirCache.newInCore();

    DirCacheBuilder builder = cache.builder();
    String[] files = new String[] {"a/b/c1.txt", "a/b/c2.txt", "a/d/c3.txt", "a/d/c4.txt", "a/c5.txt", "a/c6.txt"};
    for(String file : files)
      DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, file, ObjectId.zeroId());
    builder.finish();

    DirCacheEditor editor = cache.editor();
    DirCacheHelper.deleteDirectory(editor, "a/b");
    DirCacheHelper.deleteDirectory(editor, "a/d");
    editor.finish();

    Assert.assertEquals(2, cache.getEntryCount());
    Assert.assertNotNull(cache.getEntry("a/c5.txt"));
    Assert.assertNotNull(cache.getEntry("a/c6.txt"));
  }

  @Test
  public void isNonEmptyDirectoryTest() {
    DirCache cache = DirCache.newInCore();

    DirCacheBuilder builder = cache.builder();
    String[] files = new String[] {"a/b/c1.txt", "a/b/c2.txt", "a/d/c3.txt", "a/d/c4.txt", "a/c5.txt", "a/c6.txt"};
    for(String file : files)
      DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, file, ObjectId.zeroId());
    builder.finish();

    Assert.assertTrue(DirCacheHelper.isNonTrivialDirectory(cache, "a"));
    Assert.assertTrue(DirCacheHelper.isNonTrivialDirectory(cache, "a/b"));
    Assert.assertFalse(DirCacheHelper.isNonTrivialDirectory(cache, "a/c"));
    Assert.assertTrue(DirCacheHelper.isNonTrivialDirectory(cache, "a/d"));
  }

  @Test
  public void iterateDirectoryTest() {
    DirCache cache = DirCache.newInCore();

    DirCacheBuilder builder = cache.builder();
    String[] files = new String[] {"a/b/c1.txt", "a/b/c2.txt", "a/b/c/d3.txt", "a/d/c3.txt", "a/d/c4.txt", "a/d/e/c7.txt", "a/d/e/c8.txt", "a/d/c4/e.txt", "a/c5.txt", "a/c6.txt"};
    for(String file : files)
      DirCacheHelper.addFile(builder, FileMode.REGULAR_FILE, file, ObjectId.zeroId());
    builder.finish();

    Iterator<VirtualDirCacheEntry> iterator = DirCacheHelper.iterateDirectory(cache, "a");
    Assert.assertNotNull(iterator);
    String[] children = new String[] {"a/b", "a/c5.txt", "a/c6.txt", "a/d"};
    boolean[] isFile = new boolean[] {false, true, true, false};
    for(int i = 0; i < children.length; i++) {
      Assert.assertTrue(iterator.hasNext());
      VirtualDirCacheEntry entry = iterator.next();
      Assert.assertEquals(children[i], entry.getPath());
      Assert.assertEquals(isFile[i], entry.isRegularFile());
    }
    Assert.assertFalse(iterator.hasNext());

    iterator = DirCacheHelper.iterateDirectory(cache, "a/d");
    Assert.assertNotNull(iterator);
    children = new String[] {"a/d/c3.txt", "a/d/c4.txt", "a/d/c4", "a/d/e"};
    isFile = new boolean[] {true, true, false, false};
    for(int i = 0; i < children.length; i++) {
      Assert.assertTrue(iterator.hasNext());
      VirtualDirCacheEntry entry = iterator.next();
      Assert.assertEquals(children[i], entry.getPath());
      Assert.assertEquals(isFile[i], entry.isRegularFile());
    }
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  public void iterateNonExistentDirectoryTest() {
    DirCache cache = DirCache.newInCore();
    Iterator<VirtualDirCacheEntry> iterator = DirCacheHelper.iterateDirectory(cache, "a");
    Assert.assertNull(iterator);
  }


}
