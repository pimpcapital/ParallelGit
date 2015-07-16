package com.beijunyi.parallelgit.filesystem.utils;

import java.io.IOException;
import java.net.URI;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitFileSystemBuilderTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initFileRepository(true);
  }

  @Test
  public void buildFromUri() throws IOException{
    URI uri = GitUriBuilder.prepare()
                .repository(repo)
                .build();
    GitFileSystem gfs = GitFileSystemBuilder.forUri(uri, GitParams.emptyMap())
                          .build();
    Assert.assertEquals(repo.getDirectory(), gfs.getRepository().getDirectory());
  }
}
