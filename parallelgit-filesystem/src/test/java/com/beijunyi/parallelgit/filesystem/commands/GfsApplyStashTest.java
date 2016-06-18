package com.beijunyi.parallelgit.filesystem.commands;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.commands.GfsApplyStash.Result;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.Gfs.*;
import static java.nio.file.Files.readAllBytes;
import static org.eclipse.jgit.util.RawParseUtils.decode;
import static org.junit.Assert.*;

public class GfsApplyStashTest extends AbstractGitFileSystemTest {

  @Before
  public void setUp() throws IOException {
    initFileRepository(true);
    RepositoryUtils.setRefLogEnabled(true, repo);
    initGitFileSystem();
  }

  @Test
  public void applyStash_theLatestStashedChangesShouldAppearInTheFileSystem() throws IOException {
    byte[] expected = someBytes();
    writeToGfs("/test_file.txt", expected);
    createStash(gfs).execute();
    reset(gfs).execute();
    Result result = applyStash(gfs).execute();

    assertTrue(result.isSuccessful());
    assertArrayEquals(expected, readAllBytes(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void applySpecificStash_theCorrespondingStashedChangesShouldAppearInTheFileSystem() throws IOException {
    byte[] expected = someBytes();
    writeToGfs("/test_file.txt", expected);
    createStash(gfs).execute();
    reset(gfs).execute();

    writeSomethingToGfs();
    createStash(gfs).execute();
    reset(gfs).execute();

    Result result = applyStash(gfs).stash(1).execute();

    assertTrue(result.isSuccessful());
    assertArrayEquals(expected, readAllBytes(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void applyStashWithConflicts_theResultShouldContainTheConflicts() throws IOException {
    writeToGfs("/test_file.txt", someBytes());
    createStash(gfs).execute();
    reset(gfs).execute();

    writeToGfs("/test_file.txt", someBytes());
    Gfs.commit(gfs).execute();

    Result result = applyStash(gfs).execute();

    assertTrue(result.hasConflicts());
    assertTrue(result.getConflicts().containsKey("/test_file.txt"));
  }

  @Test
  public void applyStashWithConflicts_theConflictingFileShouldBeFormatted() throws IOException {
    writeToGfs("/test_file.txt", "version A");
    createStash(gfs).execute();
    reset(gfs).execute();

    writeToGfs("/test_file.txt", "version B");
    Gfs.commit(gfs).execute();

    Result result = applyStash(gfs).execute();

    assertTrue(result.hasConflicts());
    assertEquals("<<<<<<< Updated upstream\n" +
                 "version B\n" +
                 "=======\n" +
                 "version A\n" +
                 ">>>>>>> Stashed changes\n", decode(readAllBytes(gfs.getPath("/test_file.txt"))));
  }

}
