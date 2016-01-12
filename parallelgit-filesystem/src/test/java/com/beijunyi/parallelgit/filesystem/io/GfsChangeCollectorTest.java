package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.Files;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.utils.ObjectUtils;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.lib.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GfsChangeCollectorTest extends AbstractGitFileSystemTest {

  private GfsChangeCollector collector;

  @Before
  public void setUp() {
    collector = new GfsChangeCollector();
  }

  @Test
  public void collectAndApplyInsertion_theNewFileShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem();
    GitFileEntry entry = someFileEntry();
    collector.addChange("/test_file.txt", entry);
    collector.applyTo(gfs);
    assertTrue(Files.isRegularFile(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void collectAndApplyMultipleInsertionsInDifferentDirectories_theNewFilesShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem("/dir/some_existing_file.txt");
    GitFileEntry entry = someFileEntry();
    collector.addChange("/test_file1.txt", entry);
    collector.addChange("/dir/test_file2.txt", entry);
    collector.applyTo(gfs);
    assertTrue(Files.isRegularFile(gfs.getPath("/test_file1.txt")));
    assertTrue(Files.isRegularFile(gfs.getPath("/dir/test_file2.txt")));
  }

  @Test
  public void collectAndApplyDirectoryInsertion_theNewDirectoryShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem();
    GitFileEntry entry = someDirectoryEntry();
    collector.addChange("/test_dir", entry);
    collector.applyTo(gfs);
    assertTrue(Files.isDirectory(gfs.getPath("/test_dir")));
  }

  @Test
  public void collectAndApplyFileDeletion_theFileShouldNotExistAfterTheOperation() throws IOException {
    initGitFileSystem("/test_file.txt");
    collector.addChange("/test_file.txt", deletion());
    collector.applyTo(gfs);
    assertFalse(Files.exists(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void collectAndApplyDirectoryDeletion_theDirectoryShouldNotExistAfterTheOperation() throws IOException {
    initGitFileSystem("/test_dir/some_file.txt");
    collector.addChange("/test_dir", deletion());
    collector.applyTo(gfs);
    assertFalse(Files.exists(gfs.getPath("/test_dir")));
  }

  @Test
  public void collectAndApplyFileUpdate_theFileShouldHaveTheSpecifiedContentAfterTheOperation() throws IOException {
    initGitFileSystem("/test_file.txt");
    byte[] expected = someBytes();
    collector.addChange("/test_file.txt", newFileEntry(expected));
    collector.applyTo(gfs);
    assertArrayEquals(expected, Files.readAllBytes(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void collectAndApplyDirectoryUpdate_theFileShouldHaveTheSpecifiedContentChildrenTheOperation() throws IOException {
    initGitFileSystem("/test_dir/some_file.txt");
    collector.addChange("/test_dir", newDirectoryEntry("child1.txt", "child2.txt"));
    collector.applyTo(gfs);
    assertTrue(Files.exists(gfs.getPath("/test_dir/child1.txt")));
    assertTrue(Files.exists(gfs.getPath("/test_dir/child2.txt")));
  }

  @Nonnull
  private GitFileEntry newFileEntry(@Nonnull byte[] bytes) throws IOException {
    AnyObjectId blobId = ObjectUtils.insertBlob(bytes, repo);
    return new GitFileEntry(blobId, FileMode.REGULAR_FILE);
  }

  @Nonnull
  private GitFileEntry someFileEntry() throws IOException {
    return newFileEntry(someBytes());
  }

  @Nonnull
  private GitFileEntry deletion() {
    return new GitFileEntry(ObjectId.zeroId(), FileMode.MISSING);
  }

  @Nonnull
  private GitFileEntry newDirectoryEntry(@Nonnull String... children) throws IOException {
    TreeFormatter tf = new TreeFormatter();
    for(String child : children) {
      GitFileEntry childEntry = someFileEntry();
      tf.append(child, childEntry.getMode(), childEntry.getId());
    }
    AnyObjectId treeId = ObjectUtils.insertTree(tf, repo);
    return new GitFileEntry(treeId, FileMode.TREE);
  }

  @Nonnull
  private GitFileEntry someDirectoryEntry() throws IOException {
    return newDirectoryEntry(someFilename());
  }

}
