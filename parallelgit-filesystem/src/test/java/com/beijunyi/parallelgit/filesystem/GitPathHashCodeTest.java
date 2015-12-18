package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitPathHashCodeTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void hashCodesFromSameAbsolutePath() {
    GitPath p1 = gfs.getPath("/a/b/c");
    GitPath p2 = gfs.getPath("/a/b/c");
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void hashCodesFromSameRelativePath() {
    GitPath p1 = gfs.getPath("a/b/c");
    GitPath p2 = gfs.getPath("a/b/c");
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void hashCodesFromDifferentPaths() {
    GitPath path = gfs.getPath("/a/b/c");
    int hashCode = path.hashCode();
    assertNotEquals(hashCode, gfs.getPath("a/b/c").hashCode());
    assertNotEquals(hashCode, gfs.getPath("/a/b").hashCode());
    assertNotEquals(hashCode, gfs.getPath("/a/b/c/d").hashCode());
    assertNotEquals(hashCode, gfs.getPath("abc").hashCode());
    assertNotEquals(hashCode, gfs.getPath("/").hashCode());
    assertNotEquals(hashCode, gfs.getPath("").hashCode());
  }

  @Test
  public void hashCodesFromDifferentFileSystems() throws IOException {
    try(GitFileSystem otherGfs = Gfs.newFileSystem(repo)) {
      GitPath p1 = gfs.getPath("/a/b/c");
      GitPath p2 = otherGfs.getPath("/a/b/c");
      assertNotEquals(p1.hashCode(), p2.hashCode());
    }

  }

}
