package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.Files;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.utils.BlobUtils;
import com.beijunyi.parallelgit.utils.TreeUtils;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.TreeFormatter;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.io.GitFileEntry.*;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;
import static org.junit.Assert.*;

public class GfsChangesCollectorTest extends AbstractGitFileSystemTest {

  private GfsChangesCollector changes;

  @Before
  public void setUp() {
    changes = new GfsChangesCollector();
  }

  @Test
  public void collectAndApplyInsertion_theNewFileShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem();
    GitFileEntry entry = someFileEntry();
    changes.addChange("/test_file.txt", entry);
    changes.applyTo(gfs);
    assertTrue(Files.isRegularFile(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void collectAndApplyMultipleInsertionsInDifferentDirectories_theNewFilesShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem("/dir/some_existing_file.txt");
    GitFileEntry entry = someFileEntry();
    changes.addChange("/test_file1.txt", entry);
    changes.addChange("/dir/test_file2.txt", entry);
    changes.applyTo(gfs);
    assertTrue(Files.isRegularFile(gfs.getPath("/test_file1.txt")));
    assertTrue(Files.isRegularFile(gfs.getPath("/dir/test_file2.txt")));
  }

  @Test
  public void collectAndApplyDirectoryInsertion_theNewDirectoryShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem();
    GitFileEntry entry = someDirectoryEntry();
    changes.addChange("/test_dir", entry);
    changes.applyTo(gfs);
    assertTrue(Files.isDirectory(gfs.getPath("/test_dir")));
  }

  @Test
  public void collectAndApplyFileDeletion_theFileShouldNotExistAfterTheOperation() throws IOException {
    initGitFileSystem("/test_file.txt");
    changes.addChange("/test_file.txt", deletion());
    changes.applyTo(gfs);
    assertFalse(Files.exists(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void collectAndApplyDirectoryDeletion_theDirectoryShouldNotExistAfterTheOperation() throws IOException {
    initGitFileSystem("/test_dir/some_file.txt");
    changes.addChange("/test_dir", deletion());
    changes.applyTo(gfs);
    assertFalse(Files.exists(gfs.getPath("/test_dir")));
  }

  @Test
  public void collectAndApplyFileUpdate_theFileShouldHaveTheSpecifiedContentAfterTheOperation() throws IOException {
    initGitFileSystem("/test_file.txt");
    byte[] expected = someBytes();
    changes.addChange("/test_file.txt", newFileEntry(expected));
    changes.applyTo(gfs);
    assertArrayEquals(expected, Files.readAllBytes(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void collectAndApplyDirectoryUpdate_theDirectoryShouldHaveTheSpecifiedChildrenAfterTheOperation() throws IOException {
    initGitFileSystem("/test_dir/some_file.txt");
    changes.addChange("/test_dir", newDirectoryEntry("child1.txt", "child2.txt"));
    changes.applyTo(gfs);
    assertTrue(Files.exists(gfs.getPath("/test_dir/child1.txt")));
    assertTrue(Files.exists(gfs.getPath("/test_dir/child2.txt")));
  }

  @Test
  public void collectAndApplyChangingFileToDirectory_theDirectoryShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem("/test_target");
    changes.addChange("/test_target", newDirectoryEntry("child1.txt", "child2.txt"));
    changes.applyTo(gfs);
    assertTrue(Files.isDirectory(gfs.getPath("/test_target")));
  }

  @Test
  public void collectAndApplyChangingDirectoryToFile_theFileShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem("/test_target/some_file.txt");
    changes.addChange("/test_target", someFileEntry());
    changes.applyTo(gfs);
    assertTrue(Files.isRegularFile(gfs.getPath("/test_target")));
  }

  @Test
  public void collectAndApplyFileBytesChange_theFileShouldExistAfterTheOperation() throws IOException {
    initGitFileSystem();
    changes.addChange("/test_file.txt", someBytes(), REGULAR_FILE);
    changes.applyTo(gfs);
    assertTrue(Files.exists(gfs.getPath("/test_file.txt")));
  }

  @Test
  public void collectAndApplyFileBytesChange_theFileShouldHaveTheSpecifiedData() throws IOException {
    initGitFileSystem();
    byte[] expected = someBytes();
    changes.addChange("/test_file.txt", expected, REGULAR_FILE);
    changes.applyTo(gfs);
    assertArrayEquals(expected, Files.readAllBytes(gfs.getPath("/test_file.txt")));
  }

  @Nonnull
  private GitFileEntry newFileEntry(byte[] bytes) throws IOException {
    ObjectId blobId = BlobUtils.insertBlob(bytes, repo);
    return newEntry(blobId, REGULAR_FILE);
  }

  @Nonnull
  private GitFileEntry someFileEntry() throws IOException {
    return newFileEntry(someBytes());
  }

  @Nonnull
  private GitFileEntry deletion() {
    return missingEntry();
  }

  @Nonnull
  private GitFileEntry newDirectoryEntry(String... children) throws IOException {
    TreeFormatter tf = new TreeFormatter();
    for(String child : children) {
      GitFileEntry childEntry = someFileEntry();
      tf.append(child, childEntry.getMode(), childEntry.getId());
    }
    ObjectId treeId = TreeUtils.insertTree(tf, repo);
    return newTreeEntry(treeId);
  }

  @Nonnull
  private GitFileEntry someDirectoryEntry() throws IOException {
    return newDirectoryEntry(someFilename());
  }

}
