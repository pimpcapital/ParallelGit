package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.exceptions.GfsCheckoutConflictException;
import com.beijunyi.parallelgit.filesystem.test.NioUtils;
import com.beijunyi.parallelgit.utils.BlobUtils;
import com.beijunyi.parallelgit.utils.CacheUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

import static java.nio.file.Files.createDirectories;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;
import static org.junit.Assert.*;

/**
 * Scenarios:
 *
 *  TABLE 1:
 *   H -> HEAD;  T -> TARGET;  W -> WORKTREE
 *   Y -> YES;  N -> NO
 *    CASE     H==T     T==W     H==W       RESULT
 *     1        N        N        N       CHECK TYPE (GO TO TABLE 2)
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
 *    1-4       F        D        D      ENTER SUBTREE (GO TO TABLE 1)
 *    1-5       D        F        F        CONFLICT
 *    1-6       D        F        D        CONFLICT
 *    1-7       D        D        F        CONFLICT
 *    1-8       D        D        D      ENTER SUBTREE (GO TO TABLE 1)
 */
public class GfsDefaultCheckoutTreeTest extends AbstractGitFileSystemTest {

  @Test(expected = GfsCheckoutConflictException.class)
  public void case11_allTreesHaveDifferentFiles_shouldThrowGfsCheckoutConflictException() throws IOException {
    initGitFileSystem("/test_file.txt");
    clearWorktreeAndWrite("/test_file.txt", someBytes());
    AnyObjectId target = createTreeWithFile("/test_file.txt", someBytes());
    new GfsDefaultCheckout(gfs).checkout(target);
  }

  @Test(expected = GfsCheckoutConflictException.class)
  public void case12_headHasFile_targetHasDirectory_worktreeHasFile_shouldThrowGfsCheckoutConflictException() throws IOException {
    initGitFileSystem("/test_target");
    clearWorktreeAndWrite("/test_target/some_file.txt", someBytes());
    AnyObjectId target = createTreeWithFile("/test_target", someBytes());
    new GfsDefaultCheckout(gfs).checkout(target);
  }

  @Test(expected = GfsCheckoutConflictException.class)
  public void case13_headHasFile_targetHasFile_worktreeHasDirectory_shouldThrowGfsCheckoutConflictException() throws IOException {
    initGitFileSystem("/test_target");
    clearWorktreeAndWrite("/test_target", someBytes());
    AnyObjectId target = createTreeWithFile("/test_target/some_file.txt", someBytes());
    new GfsDefaultCheckout(gfs).checkout(target);
  }

  @Test
  public void case14_targetAndWorktreeHaveNonConflictingDirectories_theFilesFromBothDirectoriesShouldBePresentAfterTheOperation() throws IOException {
    initGitFileSystem("/test_target");
    clearWorktreeAndWrite("/test_target/some_file1.txt", someBytes());
    AnyObjectId target = createTreeWithFile("/test_target/some_file2.txt", someBytes());
    new GfsDefaultCheckout(gfs).checkout(target);
    assertTrue(Files.exists(gfs.getPath("/test_target/some_file1.txt")));
    assertTrue(Files.exists(gfs.getPath("/test_target/some_file2.txt")));
  }

  @Test(expected = GfsCheckoutConflictException.class)
  public void case15_headHasDirectory_targetAndWorktreeHaveDifferentFiles_shouldThrowGfsCheckoutConflictException() throws IOException {
    initGitFileSystem("/test_target/some_file.txt");
    clearWorktreeAndWrite("/test_target", someBytes());
    AnyObjectId target = createTreeWithFile("/test_target", someBytes());
    new GfsDefaultCheckout(gfs).checkout(target);
  }

  @Test(expected = GfsCheckoutConflictException.class)
  public void case16_headHasDirectory_targetHasFile_worktreeHasDirectory_shouldThrowGfsCheckoutConflictException() throws IOException {
    initGitFileSystem("/test_target/some_file.txt");
    clearWorktreeAndWrite("/test_target", someBytes());
    AnyObjectId target = createTreeWithFile("/test_target/some_file.txt", someBytes());
    new GfsDefaultCheckout(gfs).checkout(target);
  }

  @Test(expected = GfsCheckoutConflictException.class)
  public void case17_headHasDirectory_targetHasDirectory_worktreeHasFile_shouldThrowGfsCheckoutConflictException() throws IOException {
    initGitFileSystem("/test_target/some_file.txt");
    clearWorktreeAndWrite("/test_target/some_file.txt", someBytes());
    AnyObjectId target = createTreeWithFile("/test_target", someBytes());
    new GfsDefaultCheckout(gfs).checkout(target);
  }

  @Test
  public void case18_allTreesHaveDifferentButNonConflictingDirectories_theFilesFromTargetAndDirectoryShouldBePresentAfterTheOperation() throws IOException {
    initGitFileSystem("/test_target/some_file1.txt");
    clearWorktreeAndWrite("/test_target/some_file2.txt", someBytes());
    AnyObjectId target = createTreeWithFile("/test_target/some_file3.txt", someBytes());
    new GfsDefaultCheckout(gfs).checkout(target);
    assertFalse(Files.exists(gfs.getPath("/test_target/some_file1.txt")));
    assertTrue(Files.exists(gfs.getPath("/test_target/some_file2.txt")));
    assertTrue(Files.exists(gfs.getPath("/test_target/some_file3.txt")));
  }

  @Test
  public void case2_headNotEqualTarget_headEqualWorktree_targetFileShouldBeUsed() throws IOException {
    initGitFileSystem("/test_file.txt");
    byte[] expected = someBytes();
    AnyObjectId target = createTreeWithFile("/test_file.txt", expected);
    new GfsDefaultCheckout(gfs).checkout(target);
    assertArrayEquals(expected, Files.readAllBytes(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void case3_headNotEqualTarget_targetEqualWorktree_worktreeFileShouldRemainTheSame() throws IOException {
    initGitFileSystem("/test_file.txt");
    byte[] expected = someBytes();
    clearWorktreeAndWrite("/test_file.txt", expected);
    AnyObjectId target = createTreeWithFile("/test_file.txt", expected);
    new GfsDefaultCheckout(gfs).checkout(target);
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
    clearWorktreeAndWrite("/test_file.txt", expected);
    AnyObjectId target = createTreeWithFile("/test_file.txt", someBytes);

    new GfsDefaultCheckout(gfs).checkout(target);
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
    new GfsDefaultCheckout(gfs).checkout(target);
    assertArrayEquals(expected, Files.readAllBytes(gfs.getPath("/test_file.txt")));
  }

  private void clearWorktree() throws IOException {
    Files.walkFileTree(gfs.getRootPath(), NioUtils.RECURSIVE_DELETE);
  }

  private void clearWorktreeAndWrite(String path, byte[] bytes) throws IOException {
    clearWorktree();
    Path file = gfs.getPath(path);
    Path parent = file.getParent();
    if(parent != null) createDirectories(parent);
    Files.write(file, bytes);
  }

  @Nonnull
  private ObjectId createTreeWithFile(String path, byte[] bytes) throws IOException {
    DirCache cache = DirCache.newInCore();
    CacheUtils.addFile(path, REGULAR_FILE, BlobUtils.insertBlob(bytes, repo), cache);
    return CacheUtils.writeTree(cache, repo);
  }

}
