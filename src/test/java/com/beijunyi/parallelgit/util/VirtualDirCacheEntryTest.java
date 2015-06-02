package com.beijunyi.parallelgit.util;

import org.junit.Assert;
import org.junit.Test;

public class VirtualDirCacheEntryTest {

  @Test
  public void directoryEntryTest() {
    VirtualDirCacheEntry e = VirtualDirCacheEntry.directory("dir");
    Assert.assertTrue(e.isDirectory());
    Assert.assertFalse(e.isRegularFile());
  }

  @Test
  public void fileEntryTest() {
    VirtualDirCacheEntry e = VirtualDirCacheEntry.file("file");
    Assert.assertFalse(e.isDirectory());
    Assert.assertTrue(e.isRegularFile());
  }

  @Test
  public void entryDirPathTest() {
    String dirPath = "p/a/t/h";
    VirtualDirCacheEntry e = VirtualDirCacheEntry.directory(dirPath);
    Assert.assertEquals(dirPath, e.getPath());
  }

  @Test
  public void entryFilePathTest() {
    String filePath = "p/a/t/h.txt";
    VirtualDirCacheEntry e = VirtualDirCacheEntry.file(filePath);
    Assert.assertEquals(filePath, e.getPath());
  }

  @Test
  public void entryDirNameTest() {
    String parentPath = "p/a/t";
    String name = "h";
    String dirPath = parentPath + "/" + name;
    VirtualDirCacheEntry e = VirtualDirCacheEntry.directory(dirPath);
    Assert.assertEquals(name, e.getName());
  }

  @Test
  public void entryFileNameTest() {
    String parentPath = "p/a/t";
    String name = "h.txt";
    String filePath = parentPath + "/" + name;
    VirtualDirCacheEntry e = VirtualDirCacheEntry.file(filePath);
    Assert.assertEquals(name, e.getName());
  }
}
