package com.beijunyi.parallelgit.utils;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.Assert;
import org.junit.Test;

public class CommitHelperTest extends AbstractParallelGitTest {

  @Test
  public void convertShortMessageWhenTheFullMessageHasOnlyOneLine_shouldReturnTheFullMessage() {
    String fullMessage = "one line full message";
    Assert.assertEquals(fullMessage, CommitHelper.toShortMessage(fullMessage));
  }

  @Test
  public void convertShortMessageWhenTheFullMessageHasMultipleLines_LineBreaksShouldBeReplacedBySpaces() {
    String fullMessage = "first line\nsecond line";
    Assert.assertEquals("first line second line", CommitHelper.toShortMessage(fullMessage));
  }


}
