package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Test;

/**
 * Scenarios:
 *
 *  TABLE 1:
 *   H -> HEAD;  T -> TARGET;  W -> WORKTREE
 *   Y -> YES;  N -> NO
 *    CASE     H==T     T==W     H==W       RESULT
 *     1        N        N        N        TABLE 2
 *     2        N        N        Y         USE T
 *     3        N        Y        N         USE W
 *     4        N        Y        Y       IMPOSSIBLE
 *     5        Y        N        N         USE W
 *     6        Y        N        Y       IMPOSSIBLE
 *     7        Y        Y        N         USE W
 *     8        Y        Y        Y          OK
 *
 *  TABLE 2:
 *   F -> FILE && NON-EXISTENT;  D -> DIRECTORY
 *    CASE     HEAD    TARGET  WORKTREE     RESULT
 *     1        F        F        F        CONFLICT
 *     2        F        F        D        CONFLICT
 *     3        F        D        F        CONFLICT
 *     4        F        D        D      ENTER SUBTREE
 *     5        D        F        F        CONFLICT
 *     6        D        F        D        CONFLICT
 *     7        D        D        F        CONFLICT
 *     8        D        D        D      ENTER SUBTREE
 */
public class GfsCheckoutTest extends AbstractGitFileSystemTest {

  @Test
  public void _() throws IOException {

  }

}
