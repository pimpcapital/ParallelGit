package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.Files;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.utils.CacheUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.TreeUtils.normalizeNodePath;
import static java.util.Collections.singleton;
import static org.eclipse.jgit.dircache.DirCacheEntry.*;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;
import static org.junit.Assert.*;

public class GfsDefaultCheckoutCacheTest extends AbstractGitFileSystemTest {

  @Test
  public void checkoutCacheWithNonConflictingFileAtRootLevel_theFileShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem("/some_existing_file.txt");
    DirCache cache = createCacheWithFile("/test_file.txt");
    new GfsDefaultCheckout(gfs).checkout(cache);
    assertTrue(Files.exists(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void checkoutCacheWithNonConflictingFileInDirectory_theFileShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem("/some_existing_file.txt");
    DirCache cache = createCacheWithFile("/dir/test_file.txt");
    new GfsDefaultCheckout(gfs).checkout(cache);
    assertTrue(Files.exists(gfs.getPath("/dir/test_file.txt")));
  }

  @Test(expected = IllegalStateException.class)
  public void checkoutCacheWithMultiStagesFile_shouldThrowIllegalStateException() throws IOException {
    initGitFileSystem("/some_existing_file.txt");

    DirCache cache = DirCache.newInCore();
    DirCacheBuilder builder = cache.builder();
    builder.add(someEntry("/test_file.txt", STAGE_1));
    builder.add(someEntry("/test_file.txt", STAGE_2));
    builder.add(someEntry("/test_file.txt", STAGE_3));
    builder.finish();

    new GfsDefaultCheckout(gfs).checkout(cache);
  }

  @Test
  public void checkoutCacheWithIgnoringSomeFile_theIgnoredFileShouldNotBeCheckedOut() throws IOException {
    initGitFileSystem("/some_existing_file.txt");

    DirCache cache = DirCache.newInCore();
    DirCacheBuilder builder = cache.builder();
    builder.add(someEntry("/test_file1.txt"));
    builder.add(someEntry("/test_file2.txt"));
    builder.add(someEntry("/test_file3.txt"));
    builder.finish();
    new GfsDefaultCheckout(gfs).ignoredFiles(singleton("/test_file2.txt")).checkout(cache);

    assertTrue(Files.exists(gfs.getPath("/test_file1.txt")));
    assertFalse(Files.exists(gfs.getPath("/test_file2.txt")));
    assertTrue(Files.exists(gfs.getPath("/test_file3.txt")));
  }

  @Test
  public void checkoutCacheWithIgnoringMultiStagesFile_theIgnoredFileShouldNotBeCheckedOut() throws IOException {
    initGitFileSystem("/some_existing_file.txt");

    DirCache cache = DirCache.newInCore();
    DirCacheBuilder builder = cache.builder();
    builder.add(someEntry("/test_file.txt", STAGE_1));
    builder.add(someEntry("/test_file.txt", STAGE_2));
    builder.add(someEntry("/test_file.txt", STAGE_3));
    builder.finish();
    new GfsDefaultCheckout(gfs).ignoredFiles(singleton("/test_file.txt")).checkout(cache);

    assertFalse(Files.exists(gfs.getPath("/test_file.txt")));
  }

  @Nonnull
  private DirCache createCacheWithFile(String path) throws IOException {
    DirCache cache = DirCache.newInCore();
    CacheUtils.addFile(path, REGULAR_FILE, someObjectId(), cache);
    return cache;
  }

  @Nonnull
  private DirCacheEntry someEntry(String path, int stage) {
    DirCacheEntry ret = new DirCacheEntry(normalizeNodePath(path), stage);
    ret.setFileMode(REGULAR_FILE);
    ret.setObjectId(someObjectId());
    return ret;
  }

  @Nonnull
  private DirCacheEntry someEntry(String path) {
    return someEntry(path, STAGE_0);
  }


}
