package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.Files;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.exceptions.GfsCheckoutConflictException;
import com.beijunyi.parallelgit.utils.CacheUtils;
import com.beijunyi.parallelgit.utils.ObjectUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Scenarios:
 *
 *  TABLE 1:
 *   H -> HEAD;  T -> TARGET;  W -> WORKTREE
 *   Y -> YES;  N -> NO
 *    CASE     H==T     T==W     H==W       RESULT
 *     1        N        N        N        TABLE 2
 *     2        N        N        Y         USE T
 *     3        N        Y        N         USE W (NO CHANGE)
 *     4*       N        Y        Y       IMPOSSIBLE
 *     5        Y        N        N         USE W (NO CHANGE)
 *     6*       Y        N        Y       IMPOSSIBLE
 *     7*       Y        Y        N       IMPOSSIBLE
 *     8        Y        Y        Y          OK (NO CHANGE)
 *
 *  TABLE 2:
 *   F -> FILE && NON-EXISTENT;  D -> DIRECTORY
 *    CASE     HEAD    TARGET  WORKTREE     RESULT
 *    1-1       F        F        F        CONFLICT
 *    1-2       F        F        D        CONFLICT
 *    1-3       F        D        F        CONFLICT
 *    1-4       F        D        D      ENTER SUBTREE
 *    1-5       D        F        F        CONFLICT
 *    1-6       D        F        D        CONFLICT
 *    1-7       D        D        F        CONFLICT
 *    1-8       D        D        D      ENTER SUBTREE
 */
public class GfsCheckoutTest extends AbstractGitFileSystemTest {

  @Test(expected = GfsCheckoutConflictException.class)
  public void case11_allTreesHaveDifferentFiles() throws IOException {
    initGitFileSystem("/test_file.txt");
    Files.write(gfs.getPath("/test_file.txt"), someBytes());
    AnyObjectId target = createTreeWithFile("/test_file.txt", someBytes());
    new GfsCheckout(gfs).checkout(target);
  }

  @Test(expected = GfsCheckoutConflictException.class)
  public void case12_worktreeHasFile() throws IOException {
    initGitFileSystem("/test_target");
    Files.delete(gfs.getPath("/test_target"));
    Files.createDirectory(gfs.getPath("/test_target"));
    Files.write(gfs.getPath("/test_target/some_file.txt"), someBytes());
    AnyObjectId target = createTreeWithFile("/test_target", someBytes());
    new GfsCheckout(gfs).checkout(target);
  }

  @Test(expected = GfsCheckoutConflictException.class)
  public void case13_targetHasFile() throws IOException {
    initGitFileSystem("/test_target");
    Files.write(gfs.getPath("/test_target"), someBytes());
    AnyObjectId target = createTreeWithFile("/test_target/some_file.txt", someBytes());
    new GfsCheckout(gfs).checkout(target);
  }

  @Test
  public void case2_headNotEqualTarget_headEqualWorktree_targetFileShouldBeUsed() throws IOException {
    initGitFileSystem("/test_file.txt");
    byte[] expected = someBytes();
    AnyObjectId target = createTreeWithFile("/test_file.txt", expected);
    new GfsCheckout(gfs).checkout(target);
    assertArrayEquals(expected, Files.readAllBytes(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void case3_headNotEqualTarget_targetEqualWorktree_worktreeFileShouldRemainTheSame() throws IOException {
    initGitFileSystem("/test_file.txt");
    byte[] expected = someBytes();
    Files.write(gfs.getPath("/test_file.txt"), expected);
    AnyObjectId target = createTreeWithFile("/test_file.txt", expected);
    new GfsCheckout(gfs).checkout(target);
    assertArrayEquals(expected, Files.readAllBytes(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void case5_headEqualTarget_targetNotEqualWorktree_worktreeFileShouldRemainTheSame() throws IOException {
    initRepository();
    byte[] someBytes = someBytes();
    writeToCache("/test_file.txt", someBytes);
    commitToMaster();
    initGitFileSystem();

    byte[] expected = someBytes();
    Files.write(gfs.getPath("/test_file.txt"), expected);
    AnyObjectId target = createTreeWithFile("/test_file.txt", someBytes);

    new GfsCheckout(gfs).checkout(target);
    assertArrayEquals(expected, Files.readAllBytes(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void case8_allTreesEqual_worktreeFileShouldRemainTheSame() throws IOException {
    initRepository();
    byte[] expected = someBytes();
    writeToCache("/test_file.txt", expected);
    commitToMaster();
    initGitFileSystem();
    AnyObjectId target = createTreeWithFile("/test_file.txt", expected);
    new GfsCheckout(gfs).checkout(target);
    assertArrayEquals(expected, Files.readAllBytes(gfs.getPath("/test_file.txt")));
  }

  @Nonnull
  private AnyObjectId createTreeWithFile(@Nonnull String path, @Nonnull byte[] bytes) throws IOException {
    DirCache cache = DirCache.newInCore();
    CacheUtils.addFile(path, FileMode.REGULAR_FILE, ObjectUtils.insertBlob(bytes, repo), cache);
    return CacheUtils.writeTree(cache, repo);
  }

}
