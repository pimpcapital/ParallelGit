package usecases;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.GitPath;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class FilesReadAllBytesTest extends AbstractGitFileSystemTest {

  @Test
  public void readAllBytes_shouldReturnTheContentOfTheFileAsBytes() throws IOException {
    initRepository();
    byte[] data = someBytes();
    writeToCache("/file.txt", data);
    commitToMaster();
    initGitFileSystem();
    GitPath path = gfs.getPath("/file.txt");
    assertArrayEquals(data, Files.readAllBytes(path));
  }

  @Test(expected = NoSuchFileException.class)
  public void readAllBytesFromNonExistentFile_shouldThrowNoSuchFileException() throws IOException {
    initGitFileSystem();
    Files.readAllBytes(gfs.getPath("/non_existent_file.txt"));
  }

  @Test(expected = AccessDeniedException.class)
  public void readAllBytesFromDirectory_shouldThrowAccessDeniedException() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt", "some text");
    commitToMaster();
    initGitFileSystem();
    GitPath path = gfs.getPath("/dir");
    Files.readAllBytes(path);
  }

  @Test(expected = AccessDeniedException.class)
  public void readAllBytesFromRoot_shouldThrowAccessDeniedException() throws IOException {
    initGitFileSystem();
    Files.readAllBytes(root);
  }

}
