package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TreeUtilsNodeTypeTest extends AbstractParallelGitTest {

  @Before
  public void setUp() throws IOException {
    initMemoryRepository(true);
  }

  @Test
  public void testIsFileOrSymbolicLinkOnFile_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertTrue(TreeUtils.isFileOrSymbolicLink("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsFileOrSymbolicLinkOnSymbolicLink_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", "some link data".getBytes(), FileMode.SYMLINK);
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertTrue(TreeUtils.isFileOrSymbolicLink("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsFileOrSymbolicLinkOnDirectory_shouldReturnFalse() throws IOException {
    writeToCache("/test_dir/some_file.txt");
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isFileOrSymbolicLink("/test_dir", rootTree, repo));
  }

  @Test
  public void testIsFileOrSymbolicLinkOnNonExistentFile_shouldReturnFalse() throws IOException {
    writeSomeFileToCache();
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isFileOrSymbolicLink("/non_existent_file.txt", rootTree, repo));
  }

  @Test
  public void testIsDirectoryOnFile_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isDirectory("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsDirectoryOnDirectory_shouldReturnTrue() throws IOException {
    writeToCache("/test_dir/some_file.txt");
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertTrue(TreeUtils.isDirectory("/test_dir", rootTree, repo));
  }

  @Test
  public void testIsDirectoryOnNonExistentDirectory_shouldReturnFalse() throws IOException {
    writeSomeFileToCache();
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isDirectory("/non_existent_directory", rootTree, repo));
  }

  @Test
  public void testIsRegularFileOnRegularFile_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertTrue(TreeUtils.isRegularFile("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsRegularFileOnExecutableFile_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", "some executable data".getBytes(), FileMode.EXECUTABLE_FILE);
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isRegularFile("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsRegularFileOnNonExistentFile_shouldReturnFalse() throws IOException {
    writeSomeFileToCache();
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isRegularFile("/non_existent_file.txt", rootTree, repo));
  }

  @Test
  public void testIsExecutableFileOnRegularFile_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isExecutableFile("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsExecutableFileOnExecutableFile_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", "some executable data".getBytes(), FileMode.EXECUTABLE_FILE);
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertTrue(TreeUtils.isExecutableFile("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsExecutableFileOnNonExistentFile_shouldReturnFalse() throws IOException {
    writeSomeFileToCache();
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isExecutableFile("/non_existent_file.txt", rootTree, repo));
  }

  @Test
  public void testIsRegularOrExecutableFileOnRegularFile_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt");
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertTrue(TreeUtils.isRegularOrExecutableFile("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsRegularOrExecutableFileOnExecutableFile_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", "some executable data".getBytes(), FileMode.EXECUTABLE_FILE);
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertTrue(TreeUtils.isRegularOrExecutableFile("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsRegularOrExecutableFileOnSymbolicLink_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt", "some link data".getBytes(), FileMode.SYMLINK);
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isRegularOrExecutableFile("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsRegularOrExecutableFileOnDirectory_shouldReturnFalse() throws IOException {
    writeToCache("/test_dir/some_file.txt");
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isRegularOrExecutableFile("/test_dir", rootTree, repo));
  }

  @Test
  public void testIsRegularOrExecutableFileOnNonExistentFile_shouldReturnFalse() throws IOException {
    writeSomeFileToCache();
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isRegularOrExecutableFile("/non_existent_file.txt", rootTree, repo));
  }

  @Test
  public void testIsSymbolicLinkOnRegularFile_shouldReturnFalse() throws IOException {
    writeToCache("/test_file.txt");
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isSymbolicLink("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsSymbolicLinkOnSymbolicLink_shouldReturnTrue() throws IOException {
    writeToCache("/test_file.txt", "some link data".getBytes(), FileMode.SYMLINK);
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertTrue(TreeUtils.isSymbolicLink("/test_file.txt", rootTree, repo));
  }

  @Test
  public void testIsSymbolicLinkOnNonExistentLink_shouldReturnFalse() throws IOException {
    writeSomeFileToCache();
    AnyObjectId rootTree = commitToMaster().getTree();
    Assert.assertFalse(TreeUtils.isSymbolicLink("/non_existent_file.txt", rootTree, repo));
  }


}
