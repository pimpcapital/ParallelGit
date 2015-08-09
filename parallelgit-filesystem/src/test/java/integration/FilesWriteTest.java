package integration;

import java.io.IOException;
import java.nio.file.*;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.GitPath;
import org.eclipse.jgit.lib.Constants;
import org.junit.Assert;
import org.junit.Test;

public class FilesWriteTest extends AbstractGitFileSystemTest {

  @Test
  public void writeExistingFile_shouldOverwriteItsContent() throws IOException {
    initRepository();
    writeFile("/file.txt", "old content");
    commitToMaster();
    initGitFileSystem();
    byte[] data = Constants.encode("some plain text data");
    Path file = gfs.getPath("/file.txt");
    Files.write(file, data);
    Assert.assertArrayEquals(data, Files.readAllBytes(file));
  }

  @Test
  public void writeNonExistentFile_shouldCreateNewFile() throws IOException {
    initGitFileSystem();
    GitPath file = gfs.getPath("/file.txt");
    Files.write(file, Constants.encode("some plain text data"));
    Assert.assertTrue(Files.exists(file));
  }

  @Test(expected = AccessDeniedException.class)
  public void writeDirectory_shouldThrowAccessDeniedException() throws IOException {
    initRepository();
    writeFile("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();
    GitPath dir = gfs.getPath("/dir");
    Files.write(dir, Constants.encode("some plain text data"));
  }

  @Test(expected = AccessDeniedException.class)
  public void writeRoot_shouldThrowAccessDeniedException() throws IOException {
    initGitFileSystem();
    Files.write(root, Constants.encode("some plain text data"));
  }




}
