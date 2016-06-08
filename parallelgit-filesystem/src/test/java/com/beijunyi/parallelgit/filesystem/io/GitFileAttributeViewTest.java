package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.exceptions.IncompatibleFileModeException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.io.GfsFileAttributeView.*;
import static com.beijunyi.parallelgit.utils.TreeUtils.getObjectId;
import static org.eclipse.jgit.lib.FileMode.*;
import static org.eclipse.jgit.lib.ObjectId.zeroId;
import static org.junit.Assert.*;

public class GitFileAttributeViewTest extends AbstractGitFileSystemTest {

  @Test
  public void whenFileIsNotFreshlyAdded_isNewAttributeShouldBeFalse() throws IOException {
    initGitFileSystem("/some_file.txt");
    assertFalse(isNew("/some_file.txt"));
  }

  @Test
  public void whenFileIsFreshlyAdded_isNewAttributeShouldBeTrue() throws IOException {
    initGitFileSystem();
    Files.write(gfs.getPath("/some_file.txt"), someBytes());
    assertTrue(isNew("/some_file.txt"));
  }

  @Test
  public void whenDirectoryIsFreshlyAdded_isNewAttributeShouldBeTrue() throws IOException {
    initGitFileSystem();
    Files.createDirectory(gfs.getPath("/dir"));
    assertTrue(isNew("/dir"));
  }

  @Test
  public void whenFileIsFreshlyAddedAndThenModified_isNewAttributeShouldBeTrue() throws IOException {
    initGitFileSystem();
    Files.write(gfs.getPath("/some_file.txt"), someBytes());
    Files.write(gfs.getPath("/some_file.txt"), someBytes());
    assertTrue(isNew("/some_file.txt"));
  }

  @Test
  public void whenFileIsDeletedAndThenAdded_isNewAttributeShouldBeFalse() throws IOException {
    initGitFileSystem("/some_file.txt");
    Files.delete(gfs.getPath("/some_file.txt"));
    Files.write(gfs.getPath("/some_file.txt"), someBytes());
    assertFalse(isNew("/some_file.txt"));
  }

  @Test
  public void whenFileIsNotModified_isModifiedAttributeShouldBeFalse() throws IOException {
    initGitFileSystem("/some_file.txt");
    assertFalse(isModified("/some_file.txt"));
  }

  @Test
  public void whenFileContentIsModified_isModifiedAttributeShouldBeTrue() throws IOException {
    initGitFileSystem("/some_file.txt");
    Files.write(gfs.getPath("/some_file.txt"), someBytes());
    assertTrue(isModified("/some_file.txt"));
  }

  @Test
  public void whenFileModeIsModified_isModifiedAttributeShouldBeTrue() throws IOException {
    writeToCache("/some_file.txt", someBytes(), REGULAR_FILE);
    initGitFileSystem();
    open("/some_file.txt").setFileMode(EXECUTABLE_FILE);
    assertTrue(isModified("/some_file.txt"));
  }

  @Test
  public void whenFileContentIsModified_isModifiedAttributeOfTheParentDirectoryShouldBeTrue() throws IOException {
    initGitFileSystem("/dir/some_file.txt");
    Files.write(gfs.getPath("/dir/some_file.txt"), someBytes());
    assertTrue(isModified("/dir"));
  }

  @Test
  public void whenFileModeIsModified_isModifiedAttributeOfTheParentDirectoryShouldBeTrue() throws IOException {
    writeToCache("/dir/some_file.txt", someBytes(), REGULAR_FILE);
    initGitFileSystem();
    open("/dir/some_file.txt").setFileMode(EXECUTABLE_FILE);
    assertTrue(isModified("/dir"));
  }

  @Test
  public void whenFileIsFreshlyAddedAndThenModified_isModifiedAttributeShouldBeTrue() throws IOException {
    initGitFileSystem();
    Files.write(gfs.getPath("/some_file.txt"), someBytes());
    Files.write(gfs.getPath("/some_file.txt"), someBytes());
    assertTrue(isModified("/some_file.txt"));
  }

  @Test
  public void whenNewChildIsAdded_isModifiedAttributeOfTheDirectoryShouldBeTrue() throws IOException {
    initGitFileSystem("/dir/some_file.txt");
    Files.write(gfs.getPath("/dir/some_other_file.txt"), someBytes());
    assertTrue(isModified("/dir"));
  }

  @Test
  public void whenNewChildIsDeleted_isModifiedAttributeOfTheDirectoryShouldBeTrue() throws IOException {
    initGitFileSystem("/dir/some_file.txt");
    Files.delete(gfs.getPath("/dir/some_file.txt"));
    assertTrue(isModified("/dir"));
  }

  @Test
  public void whenEmptyChildrenDirectoriesAreCreated_isModifiedAttributeOfTheDirectoryShouldBeFalse() throws IOException {
    initGitFileSystem("/dir/some_file.txt");
    Files.createDirectories(gfs.getPath("/dir/empty_dir1/empty_dir2"));
    assertFalse(isModified("/dir"));
  }

  @Test
  public void whenFileIsDeletedAndThenAddedWithTheSameContent_isModifiedAttributeShouldBeFalse() throws IOException {
    byte[] data = someBytes();
    writeToCache("/some_file.txt", data);
    initGitFileSystem();
    Files.delete(gfs.getPath("/some_file.txt"));
    Files.write(gfs.getPath("/some_file.txt"), data);
    assertFalse(isModified("/some_file.txt"));
  }

  @Test
  public void whenFileIsDeletedAndThenAddedWithTheSameContent_isModifiedAttributeOfTheParentDirectoryShouldBeFalse() throws IOException {
    byte[] data = someBytes();
    writeToCache("/dir/some_file.txt", data);
    initGitFileSystem();
    Files.delete(gfs.getPath("/dir/some_file.txt"));
    Files.write(gfs.getPath("/dir/some_file.txt"), data);
    assertFalse(isModified("/dir"));
  }

  @Test
  public void whenFileIsNotModified_getObjectIdShouldReturnItsOriginalBlobId() throws IOException {
    writeToCache("/some_file.txt", someBytes());
    initGitFileSystem();
    RevCommit head = gfs.getStatusProvider().commit();
    assertEquals(getObjectId("/some_file.txt", head.getTree(), repo), objectId("/some_file.txt"));
  }

  @Test
  public void whenFileIsModified_getObjectIdShouldReturnItsNewBlobId() throws IOException {
    initGitFileSystem("/some_file.txt");
    byte[] data = someBytes();
    Files.write(gfs.getPath("/some_file.txt"), data);
    assertEquals(calculateBlobId(data), objectId("/some_file.txt"));
  }

  @Test
  public void whenDirectoryIsNotModified_getObjectIdShouldReturnItsOriginalBlobId() throws IOException {
    writeToCache("/dir/some_file.txt", someBytes());
    initGitFileSystem();
    RevCommit head = gfs.getStatusProvider().commit();
    assertEquals(getObjectId("/dir", head.getTree(), repo), objectId("/dir"));
  }

  @Test
  public void whenEmptyChildrenDirectoriesIAreCreated_getObjectIdShouldReturnSameValue() throws IOException {
    initGitFileSystem("/dir/some_file.txt");
    AnyObjectId original = objectId("/dir");
    Files.createDirectories(gfs.getPath("/dir/empty_dir1/empty_dir2"));
    assertEquals(original, objectId("/dir"));
  }

  @Test
  public void whenDirectoryIsEmpty_getObjectIdShouldReturnZeroId() throws IOException {
    initGitFileSystem();
    Files.createDirectory(gfs.getPath("/dir"));
    assertEquals(zeroId(), objectId("/dir"));
  }

  @Test
  public void whenDirectoryIsModified_getObjectIdShouldReturnDifferentValue() throws IOException {
    initGitFileSystem("/dir/some_file.txt");
    AnyObjectId original = objectId("/dir");
    Files.write(gfs.getPath("/dir/some_other_file.txt"), someBytes());
    assertNotEquals(original, objectId("/dir"));
  }

  @Test
  public void whenFileIsNotModified_getFileModeShouldReturnItsOriginalFileMode() throws IOException {
    writeToCache("/some_file.txt", someBytes(), EXECUTABLE_FILE);
    initGitFileSystem();
    assertEquals(EXECUTABLE_FILE, fileMode("/some_file.txt"));
  }

  @Test
  public void whenFileIsModified_getFileModeShouldReturnItsNewFileMode() throws IOException {
    writeToCache("/some_file.txt", someBytes(), REGULAR_FILE);
    initGitFileSystem();
    open("/some_file.txt").setFileMode(EXECUTABLE_FILE);
    assertEquals(EXECUTABLE_FILE, fileMode("/some_file.txt"));
  }

  @Test(expected = IncompatibleFileModeException.class)
  public void setTheModeOfDirectoryToFile_shouldThrowIncompatibleFileModeException() throws IOException {
    initGitFileSystem("/dir/some_file.txt");
    open("/dir").setFileMode(REGULAR_FILE);
  }

  @Test(expected = IncompatibleFileModeException.class)
  public void setTheModeOfFileToDirectory_shouldThrowIncompatibleFileModeException() throws IOException {
    initGitFileSystem("/some_file.txt");
    open("/some_file.txt").setFileMode(TREE);
  }

  @Nonnull
  private GfsFileAttributeView.Git open(String path) {
    GfsFileAttributeView.Git view = (GfsFileAttributeView.Git) provider.getFileAttributeView(gfs.getPath(path), GitFileAttributeView.class);
    assert view != null;
    return view;
  }

  @Nullable
  private Object readAttribute(String path, String key) throws IOException {
    return open(path).readAttributes(Collections.singleton(key)).get(key);
  }

  private boolean isNew(String path) throws IOException {
    return (boolean) readAttribute(path, IS_NEW);
  }

  private boolean isModified(String path) throws IOException {
    return (boolean) readAttribute(path, IS_MODIFIED);
  }

  @Nullable
  private ObjectId objectId(String path) throws IOException {
    return (ObjectId) readAttribute(path, OBJECT_ID);
  }

  @Nullable
  private FileMode fileMode(String path) throws IOException {
    return (FileMode) readAttribute(path, FILE_MODE);
  }



}
