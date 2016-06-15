package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static java.nio.file.Files.readAllBytes;
import static org.junit.Assert.assertArrayEquals;

public class GfsApplyStashTest extends AbstractGitFileSystemTest {

  @Before
  public void setUp() throws IOException {
    initFileRepository(true);
    RepositoryUtils.setRefLogEnabled(true, repo);
    initGitFileSystem();
  }

  @Test
  public void applyStash_theLatestStashedChangeShouldAppearInTheFileSystem() throws IOException {
    byte[] expected = someBytes();
    writeToGfs("/test_file.txt", expected);
    createStash(gfs).execute();
    reset(gfs).execute();
    applyStash(gfs).execute();
    assertArrayEquals(expected, readAllBytes(gfs.getPath("/test_file.txt")));
  }

}
