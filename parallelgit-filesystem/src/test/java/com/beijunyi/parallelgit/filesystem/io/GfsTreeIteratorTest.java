package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator.iterateRoot;
import static org.junit.Assert.*;

public class GfsTreeIteratorTest extends AbstractGitFileSystemTest {

  private GfsTreeIterator iterator;

  @Before
  public void setUp() throws IOException {
    initGitFileSystem();
    writeToGfs("/file1.txt");
    writeToGfs("/file2.txt");
    writeToGfs("/file3.txt");
    writeToGfs("/file4.txt");
    writeToGfs("/file5.txt");
    iterator = iterateRoot(gfs);
  }


  @Test
  public void createNewIterator_iteratorShouldStartFromTheFirstEntry() throws IOException {
    assertTrue(iterator.first());
  }

  @Test
  public void forwardFour_iteratorShouldBePositionedToTheFifthEntry() throws IOException {
    iterator.next(4);
    assertEquals("file5.txt", iterator.getEntryPathString());
  }

  @Test
  public void forwardFourAndBackwardTwo_iteratorShouldBePositionedToTheThirdEntry() throws IOException {
    iterator.next(4);
    iterator.back(2);
    assertEquals("file3.txt", iterator.getEntryPathString());
  }

}
