package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;

import com.beijunyi.parallelgit.filesystem.utils.GfsUriBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitFileSystemProviderGetPathTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void getPathFromUri() {
    URI uri = GfsUriBuilder.fromFileSystem(gfs)
                .file("/some_file.txt")
                .build();
    GitPath path = provider.getPath(uri);
    assertEquals(gfs.getPath("/some_file.txt"), path);
  }

  @Test
  public void getRootPathFromUri() {
    URI uri = GfsUriBuilder.fromFileSystem(gfs)
                .file("/")
                .build();
    GitPath path = provider.getPath(uri);
    assertEquals(gfs.getRootPath(), path);
  }

}
