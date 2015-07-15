package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitPathIteratorTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathIteratorTest() {
    GitPath path = gfs.getPath("/a/b/c");
    Iterator<Path> it = path.iterator();
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals("a", it.next().toString());
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals("b", it.next().toString());
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals("c", it.next().toString());
    Assert.assertFalse(it.hasNext());
  }

  @Test
  public void rootPathIteratorTest() {
    GitPath path = gfs.getPath("/");
    Iterator<Path> it = path.iterator();
    Assert.assertFalse(it.hasNext());
  }

  @Test
  public void relativePathIteratorTest() {
    GitPath path = gfs.getPath("a/b/c");
    Iterator<Path> it = path.iterator();
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals("a", it.next().toString());
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals("b", it.next().toString());
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals("c", it.next().toString());
    Assert.assertFalse(it.hasNext());
  }

  @Test
  public void emptyPathIteratorTest() {
    GitPath path = gfs.getPath("");
    Iterator<Path> it = path.iterator();
    Assert.assertFalse(it.hasNext());
  }

  @Test(expected = NoSuchElementException.class)
  public void gitPathIteratorNoSuchElementTest() {
    GitPath path = gfs.getPath("a");
    Iterator<Path> it = path.iterator();
    it.next();
    it.next();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void gitPathIteratorRemoveTest() {
    GitPath path = gfs.getPath("a");
    Iterator<Path> it = path.iterator();
    it.next();
    it.remove();
  }

}
