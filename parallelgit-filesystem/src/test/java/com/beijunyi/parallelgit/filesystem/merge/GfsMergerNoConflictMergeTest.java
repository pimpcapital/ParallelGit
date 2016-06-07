package com.beijunyi.parallelgit.filesystem.merge;

import com.beijunyi.parallelgit.AbstractParallelGitTest;

public class GfsMergerNoConflictMergeTest extends AbstractParallelGitTest {
//
//  @Before
//  public void setUp() throws IOException {
//    initRepository();
//  }
//
//  @Test
//  public void whenMergeFinishesSuccessfully_shouldReturnTrue() throws IOException {
//    RevCommit base = commit();
//    RevCommit ours = clearAndWrite("/ours.txt", base);
//    RevCommit theirs = clearAndWrite("/theirs.txt", base);
//    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
//      GfsMerger merger = new GfsMerger(gfs);
//      assertTrue(merger.merge(ours, theirs));
//    }
//  }
//
//  @Test
//  public void whenMergeFinishesSuccessfully_resultTreeIdShouldBeNotNull() throws IOException {
//    AnyObjectId base = commit();
//    AnyObjectId ours = clearAndWrite("/ours.txt", base);
//    AnyObjectId theirs = clearAndWrite("/theirs.txt", base);
//
//    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
//      GfsMerger merger = new GfsMerger(gfs);
//      merger.merge(ours, theirs);
//      assertNotNull(merger.getResultTreeId());
//    }
//  }
//
//  @Test
//  public void whenOurCommitAndTheirCommitInsertDifferentFiles_bothFilesShouldExistInTheResultTree() throws IOException {
//    AnyObjectId base = commit();
//    AnyObjectId ours = clearAndWrite("/ours.txt", base);
//    AnyObjectId theirs = clearAndWrite("/theirs.txt", base);
//    AnyObjectId result = merge(ours, theirs);
//    assertTrue(TreeUtils.exists("/ours.txt", result, repo));
//    assertTrue(TreeUtils.exists("/theirs.txt", result, repo));
//  }
//
//  @Test
//  public void whenOurCommitAndTheirCommitInsertDifferentFilesInOneDirectory_bothFilesShouldExistInTheResultTree() throws IOException {
//    AnyObjectId base = commit();
//    String dir = "/dir";
//    AnyObjectId ours = clearAndWrite(dir + "/ours.txt", base);
//    AnyObjectId theirs = clearAndWrite(dir + "/theirs.txt", base);
//    AnyObjectId result = merge(ours, theirs);
//    assertTrue(TreeUtils.exists(dir + "/ours.txt", result, repo));
//    assertTrue(TreeUtils.exists(dir + "/theirs.txt", result, repo));
//  }
//
//  @Test
//  public void whenOursAndTheirsHaveTheSameInsertion_theFileShouldExistInTheResultTree() throws IOException {
//    AnyObjectId base = commit();
//    writeToCache("/same_insertion.txt");
//    AnyObjectId ours = commit(base);
//    AnyObjectId theirs = commit(base);
//    AnyObjectId result = merge(ours, theirs);
//    assertTrue(TreeUtils.exists("/same_insertion.txt", result, repo));
//  }
//
//  @Test
//  public void whenOursAndTheirsDeleteTheSameFile_theFileShouldNotExistInTheResultTree() throws IOException {
//    writeToCache("/same_deletion.txt");
//    AnyObjectId base = commit();
//    AnyObjectId ours = clearAndWrite("/something.txt", base);
//    AnyObjectId theirs = clearAndWrite("/something_else.txt", base);
//    AnyObjectId result = merge(ours, theirs);
//    assertFalse(TreeUtils.exists("/same_deletion.txt", result, repo));
//  }
//
//  @Test
//  public void whenOursAndTheirsInsertTheSameDirectory_theDirectoryShouldExistInTheResultTree() throws IOException {
//    AnyObjectId base = commit();
//    String dir = "/dir";
//    writeToCache(dir + "/some_file.txt");
//    AnyObjectId ours = commit(base);
//    AnyObjectId theirs = commit(base);
//    AnyObjectId result = merge(ours, theirs);
//    assertTrue(TreeUtils.exists(dir, result, repo));
//  }
//
//  @Test
//  public void whenOursAndTheirsDeleteTheSameDirectory_theDirectoryShouldNotExistInTheResultTree() throws IOException {
//    String dir = "/dir";
//    writeToCache(dir + "/some_file.txt");
//    AnyObjectId base = commit();
//    AnyObjectId ours = clearAndWrite("/something.txt", base);
//    AnyObjectId theirs = clearAndWrite("/something_else.txt", base);
//    AnyObjectId result = merge(ours, theirs);
//    assertFalse(TreeUtils.exists(dir, result, repo));
//  }
//
//  @Test
//  public void whenOursAndTheirsHaveTheSameChanges_theFileInTheResultTreeShouldHaveTheNewContent() throws IOException {
//    writeToCache("/same_changes.txt", "some text data");
//    AnyObjectId base = commit();
//
//    clearCache();
//    byte[] data = "same changes".getBytes();
//    writeToCache("/same_changes.txt", data);
//    AnyObjectId ours = commit(base);
//    AnyObjectId theirs = commit(base);
//    AnyObjectId result = merge(ours, theirs);
//    assertArrayEquals(data, TreeUtils.readFile("/same_changes.txt", result, repo).getBytes());
//  }
//
//  @Test
//  public void whenOursAndTheirsBothChangeDirectoryIntoFile_theNewFileShouldExistInTheResultTree() throws IOException {
//    writeToCache("/target/some_file.txt");
//    AnyObjectId base = commit();
//
//    clearCache();
//    writeToCache("/target", "same content to avoid conflict");
//    AnyObjectId ours = commit("/target", base);
//    AnyObjectId theirs = commit("/target", base);
//    AnyObjectId result = merge(ours, theirs);
//    assertTrue(TreeUtils.isRegularFile("/target", result, repo));
//  }
//
//  @Test
//  public void whenOursAndTheirsBothChangeFileIntoDirectory_theNewDirectoryShouldExistInTheResultTree() throws IOException {
//    writeToCache("/target");
//    AnyObjectId base = commit();
//    AnyObjectId ours = clearAndWrite("/target/some_file.txt", base);
//    AnyObjectId theirs = clearAndWrite("/target/some_other_file.txt", base);
//    AnyObjectId result = merge(ours, theirs);
//    assertTrue(TreeUtils.isDirectory("/target", result, repo));
//  }
//
//  @Test
//  public void whenContentFromOursAndTheirsCanBeAutoMerged_() throws IOException {
//    writeToCache("/test_file.txt", "a\nb\nc\nd\ne");
//    RevCommit base = commit();
//
//    clearCache();
//    writeToCache("/test_file.txt", "a\nB\nc\nd\ne");
//    RevCommit ours = commit(base);
//
//    clearCache();
//    writeToCache("/test_file.txt", "a\nB\nc\nD\nd\ne");
//    RevCommit theirs = commit(base);
//
//    AnyObjectId tree;
//    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
//      GfsMerger merger = new GfsMerger(gfs);
//      merger.merge(ours, theirs);
//      tree = merger.getResultTreeId();
//    }
//    assertArrayEquals("".getBytes(), TreeUtils.readFile("/test_file.txt", tree, repo).getBytes());
//  }
//
//  @Nonnull
//  private RevCommit clearAndWrite(@Nonnull String file, AnyObjectId base) throws IOException {
//    clearCache();
//    writeToCache(file);
//    return commit(base);
//  }
//
//  @Nonnull
//  private RevCommit clearAndWrite(@Nonnull String file, byte[] bytes, AnyObjectId base) throws IOException {
//    clearCache();
//    writeToCache(file, bytes);
//    return commit(base);
//  }
//
//  @Nonnull
//  private ObjectId merge(@Nonnull AnyObjectId ours, AnyObjectId theirs) throws IOException {
//    try(GitFileSystem gfs = Gfs.newFileSystem(ours, repo)) {
//      GfsMerger merger = new GfsMerger(gfs);
//      merger.merge(ours, theirs);
//      return merger.getResultTreeId();
//    }
//  }

}
