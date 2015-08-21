package com.beijunyi.parallelgit.runtime;

import java.io.IOException;
import javax.annotation.Nullable;

import org.junit.Test;

public class ParallelCommandTest {

  @Test(expected = IllegalStateException.class)
  public void callParallelCommandTwiceTest() throws IOException {
    ParallelCommand command = new DoNothingParallelCommand();
    command.call();
    command.call();
  }

  private static class DoNothingParallelCommand extends ParallelCommand<Void> {
    @Nullable
    @Override
    protected Void doCall() throws IOException {
      return null;
    }
  }
}
