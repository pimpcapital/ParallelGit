package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.utils.GfsParams;
import com.beijunyi.parallelgit.filesystem.utils.GfsUriBuilder;
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
    URI uri = GfsUriBuilder.prepare()
                .repository(repo)
                .build();
    GitFileSystem gfs = Gfs.newFileSystem(uri, GfsParams.emptyMap());
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

  @Test
  public void buildFromPath() throws IOException {
    Path path = repoDir.toPath();
    GitFileSystem gfs = Gfs.newFileSystem(path, GfsParams.emptyMap());
    assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }

}
