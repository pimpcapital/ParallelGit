package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitPathEqualTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void hashCodeOfAbsolutePathTest() {
    GitPath p1 = gfs.getPath("/a/b/c");
    GitPath p2 = gfs.getPath("/a/b/c");
    assertTrue(p1.equals(p2));
  }

  @Test
  public void hashCodeOfRelativePathTest() {
    GitPath p1 = gfs.getPath("a/b/c");
    GitPath p2 = gfs.getPath("a/b/c");
    assertTrue(p1.equals(p2));
  }

  @Test
  public void hashCodesFromDifferentPathsTest() {
    GitPath path = gfs.getPath("/a/b/c");
    assertFalse(path.equals(gfs.getPath("a/b/c")));
    assertFalse(path.equals(gfs.getPath("/a/b")));
    assertFalse(path.equals(gfs.getPath("/a/b/c/d")));
    assertFalse(path.equals(gfs.getPath("abc")));
    assertFalse(path.equals(gfs.getPath("/")));
    assertFalse(path.equals(gfs.getPath("")));
  }

  @Test
  public void hashCodesFromDifferentFileSystemTest() throws IOException {
    GitFileSystem other = GitFileSystemBuilder.prepare()
                            .repository(repo)
                            .build();
    GitPath p1 = gfs.getPath("/a/b/c");
    GitPath p2 = other.getPath("/a/b/c");
    assertFalse(p1.equals(p2));
  }

}
