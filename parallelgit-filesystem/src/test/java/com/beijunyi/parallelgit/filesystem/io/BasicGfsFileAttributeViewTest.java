package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.eclipse.jgit.lib.FileMode;
import org.junit.Assert;
import org.junit.Test;

public class BasicGfsFileAttributeViewTest extends AbstractGitFileSystemTest {

  @Test
  public void getName_shouldReturnBasic() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals("basic", view.name());
  }

  @Nullable
  private static Object readAttribute(@Nonnull GfsFileAttributeView view, @Nonnull String attributeKey) throws IOException {
    return view.readAttributes(Collections.singleton(attributeKey)).get(attributeKey);
  }

  @Test
  public void getSizeAttributeFromFile_shouldReturnTheFileSize() throws IOException {
    initRepository();
    byte[] data = "13 bytes data".getBytes();
    writeFile("/file.txt", data);
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(13L, readAttribute(view, GfsFileAttributeView.Basic.SIZE_NAME));
  }

  @Test
  public void getSizeAttributeFromDirectory_shouldReturnZero() throws IOException {
    initRepository();
    writeFile("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/dir"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(0L, readAttribute(view, GfsFileAttributeView.Basic.SIZE_NAME));
  }

  @Test
  public void getCreationTimeAttributeFromFile_shouldReturnEpoch() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(FileTime.fromMillis(0), readAttribute(view, GfsFileAttributeView.Basic.CREATION_TIME_NAME));
  }

  @Test
  public void getLastAccessTimeAttributeFromFile_shouldReturnEpoch() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(FileTime.fromMillis(0), readAttribute(view, GfsFileAttributeView.Basic.LAST_ACCESS_TIME_NAME));
  }

  @Test
  public void getLastModifiedTimeAttributeFromFile_shouldReturnEpoch() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(FileTime.fromMillis(0), readAttribute(view, GfsFileAttributeView.Basic.LAST_MODIFIED_TIME_NAME));
  }

  @Test
  public void getFileKeyAttributeFromFile_shouldReturnNull() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertNull(readAttribute(view, GfsFileAttributeView.Basic.FILE_KEY_NAME));
  }

  @Test
  public void getIsDirectoryAttributeFromFile_shouldReturnFalse() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(false, readAttribute(view, GfsFileAttributeView.Basic.IS_DIRECTORY_NAME));
  }

  @Test
  public void getIsDirectoryAttributeFromDirectory_shouldReturnTrue() throws IOException {
    initRepository();
    writeFile("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/dir"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(true, readAttribute(view, GfsFileAttributeView.Basic.IS_DIRECTORY_NAME));
  }

  @Test
  public void getIsRegularFileAttributeFromFile_shouldReturnTrue() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(true, readAttribute(view, GfsFileAttributeView.Basic.IS_REGULAR_FILE_NAME));
  }

  @Test
  public void getIsRegularFileAttributeFromExecutableFile_shouldReturnTrue() throws IOException {
    initRepository();
    writeFile("/file.txt", "some data".getBytes(), FileMode.EXECUTABLE_FILE);
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(true, readAttribute(view, GfsFileAttributeView.Basic.IS_REGULAR_FILE_NAME));
  }

  @Test
  public void getIsRegularFileAttributeFromDirectory_shouldReturnFalse() throws IOException {
    initRepository();
    writeFile("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/dir"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(false, readAttribute(view, GfsFileAttributeView.Basic.IS_REGULAR_FILE_NAME));
  }

  @Test
  public void getIsSymbolicLinkAttributeFromFile_shouldReturnFalse() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(false, readAttribute(view, GfsFileAttributeView.Basic.IS_SYMBOLIC_LINK_NAME));
  }

  @Test
  public void getIsSymbolicLinkAttributeFromSymbolicLink_shouldReturnTrue() throws IOException {
    initRepository();
    writeFile("/file.txt" , "some link".getBytes(), FileMode.SYMLINK);
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(true, readAttribute(view, GfsFileAttributeView.Basic.IS_SYMBOLIC_LINK_NAME));
  }

  @Test
  public void getIsOtherAttributeFromFile_shouldReturnFalse() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    Assert.assertEquals(false, readAttribute(view, GfsFileAttributeView.Basic.IS_OTHER_NAME));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUnsupportedAttributeFromFile_shouldThrowUnsupportedOperationException() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    readAttribute(view, "unsupportedAttribute");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setTimes_shouldThrowUnsupportedOperationException() throws IOException {
    initRepository();
    writeFile("/file.txt");
    commitToMaster();
    initGitFileSystem();

    GfsFileAttributeView.Basic view = provider.getFileAttributeView(gfs.getPath("/file.txt"), GfsFileAttributeView.Basic.class);
    Assert.assertNotNull(view);
    FileTime now = FileTime.fromMillis(System.currentTimeMillis());
    view.setTimes(now, now, now);
  }

}
