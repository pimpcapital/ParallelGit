package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitPathIteratorTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathIteratorTest() {
    GitPath path = gfs.getPath("/a/b/c");
    Iterator<Path> it = path.iterator();
    assertTrue(it.hasNext());
    assertEquals("a", it.next().toString());
    assertTrue(it.hasNext());
    assertEquals("b", it.next().toString());
    assertTrue(it.hasNext());
    assertEquals("c", it.next().toString());
    assertFalse(it.hasNext());
  }

  @Test
  public void rootPathIteratorTest() {
    GitPath path = gfs.getPath("/");
    Iterator<Path> it = path.iterator();
    assertFalse(it.hasNext());
  }

  @Test
  public void relativePathIteratorTest() {
    GitPath path = gfs.getPath("a/b/c");
    Iterator<Path> it = path.iterator();
    assertTrue(it.hasNext());
    assertEquals("a", it.next().toString());
    assertTrue(it.hasNext());
    assertEquals("b", it.next().toString());
    assertTrue(it.hasNext());
    assertEquals("c", it.next().toString());
    assertFalse(it.hasNext());
  }

  @Test
  public void emptyPathIteratorTest() {
    GitPath path = gfs.getPath("");
    Iterator<Path> it = path.iterator();
    assertFalse(it.hasNext());
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
