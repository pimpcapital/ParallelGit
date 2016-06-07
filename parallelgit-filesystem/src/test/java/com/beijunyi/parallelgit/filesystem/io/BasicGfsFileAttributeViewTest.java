package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.eclipse.jgit.lib.FileMode;
import org.junit.Test;

import static org.junit.Assert.*;

public class BasicGfsFileAttributeViewTest extends AbstractGitFileSystemTest {

  @Test
  public void getName_shouldReturnBasic() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals("basic", view.name());
  }

  @Test
  public void readAttributes_shouldReturnNotNull() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertNotNull(view.readAttributes());
  }

  @Nullable
  public static Object readAttribute(GfsFileAttributeView view, String key) throws IOException {
    return view.readAttributes(Collections.singleton(key)).get(key);
  }

  @Test
  public void getSizeAttributeOfFile_shouldReturnTheFileSize() throws IOException {
    initRepository();
    byte[] data = "13 bytes data".getBytes();
    writeToCache("/file.txt", data);
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(13L, readAttribute(view, GfsFileAttributeView.Basic.SIZE_NAME));
  }

  @Test
  public void getSizeAttributeOfDirectory_shouldReturnZero() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/dir"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(0L, readAttribute(view, GfsFileAttributeView.Basic.SIZE_NAME));
  }

  @Test
  public void getCreationTimeAttributeOfFile_shouldReturnEpoch() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(FileTime.fromMillis(0), readAttribute(view, GfsFileAttributeView.Basic.CREATION_TIME_NAME));
  }

  @Test
  public void getLastAccessTimeAttributeOfFile_shouldReturnEpoch() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(FileTime.fromMillis(0), readAttribute(view, GfsFileAttributeView.Basic.LAST_ACCESS_TIME_NAME));
  }

  @Test
  public void getLastModifiedTimeAttributeOfFile_shouldReturnEpoch() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(FileTime.fromMillis(0), readAttribute(view, GfsFileAttributeView.Basic.LAST_MODIFIED_TIME_NAME));
  }

  @Test
  public void getFileKeyAttributeOfFile_shouldReturnNull() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertNull(readAttribute(view, GfsFileAttributeView.Basic.FILE_KEY_NAME));
  }

  @Test
  public void getIsDirectoryAttributeOfFile_shouldReturnFalse() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(false, readAttribute(view, GfsFileAttributeView.Basic.IS_DIRECTORY_NAME));
  }

  @Test
  public void getIsDirectoryAttributeOfDirectory_shouldReturnTrue() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/dir"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(true, readAttribute(view, GfsFileAttributeView.Basic.IS_DIRECTORY_NAME));
  }

  @Test
  public void getIsRegularFileAttributeOfFile_shouldReturnTrue() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(true, readAttribute(view, GfsFileAttributeView.Basic.IS_REGULAR_FILE_NAME));
  }

  @Test
  public void getIsRegularFileAttributeOfExecutableFile_shouldReturnTrue() throws IOException {
    initRepository();
    writeToCache("/file.txt", "some data".getBytes(), FileMode.EXECUTABLE_FILE);
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(true, readAttribute(view, GfsFileAttributeView.Basic.IS_REGULAR_FILE_NAME));
  }

  @Test
  public void getIsRegularFileAttributeOfDirectory_shouldReturnFalse() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/dir"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(false, readAttribute(view, GfsFileAttributeView.Basic.IS_REGULAR_FILE_NAME));
  }

  @Test
  public void getIsSymbolicLinkAttributeOfFile_shouldReturnFalse() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(false, readAttribute(view, GfsFileAttributeView.Basic.IS_SYMBOLIC_LINK_NAME));
  }

  @Test
  public void getIsSymbolicLinkAttributeOfSymbolicLink_shouldReturnTrue() throws IOException {
    initRepository();
    writeToCache("/file.txt", "some link".getBytes(), FileMode.SYMLINK);
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(true, readAttribute(view, GfsFileAttributeView.Basic.IS_SYMBOLIC_LINK_NAME));
  }

  @Test
  public void getIsOtherAttributeOfFile_shouldReturnFalse() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    assertEquals(false, readAttribute(view, GfsFileAttributeView.Basic.IS_OTHER_NAME));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getNonBasicAttributeOfFile_shouldThrowUnsupportedOperationException() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    readAttribute(view, "nonBasicAttribute");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setTimes_shouldThrowUnsupportedOperationException() throws IOException {
    initRepository();
    writeToCache("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    assertNotNull(view);
    FileTime now = FileTime.fromMillis(System.currentTimeMillis());
    view.setTimes(now, now, now);
  }

}
