package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.utils.GitParams;
import com.beijunyi.parallelgit.filesystem.utils.GitUriBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GfsFromPathTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initFileRepository(false);
  }

  @Test
  public void buildFromUri() throws IOException {
    URI uri = GitUriBuilder.prepare()
                .repository(repo)
                .build();
    GitFileSystem gfs = Gfs.fromUri(uri, GitParams.emptyMap());
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromPath() throws IOException {
    Path path = repoDir.toPath();
    GitFileSystem gfs = Gfs.fromPath(path, GitParams.emptyMap());
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

}
