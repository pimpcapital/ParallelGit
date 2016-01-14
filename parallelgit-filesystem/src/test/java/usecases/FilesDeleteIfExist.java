package usecases;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class FilesDeleteIfExist extends AbstractGitFileSystemTest {

  @Test
  public void whenFileDoesNotExist_shouldReturnFalse() throws IOException {
    initGitFileSystem();
    assertFalse(Files.deleteIfExists(gfs.getPath("non_existent.txt")));
  }

  @Test
  public void whenFileParentDirDoesNotExist_shouldReturnFalse() throws IOException {
    initGitFileSystem();
    assertFalse(Files.deleteIfExists(gfs.getPath("/non_existent/test_file.txt")));
  }

  @Test
  public void whenFileExists_shouldReturnFalse() throws IOException {
    initGitFileSystem("/some_dir/test_file.txt");
    assertTrue(Files.deleteIfExists(gfs.getPath("/some_dir/test_file.txt")));
  }

  @Test
  public void whenFileExists_theFileShouldBeDeleted() throws IOException {
    initGitFileSystem("/some_dir/test_file.txt");
    Files.deleteIfExists(gfs.getPath("/some_dir/test_file.txt"));
    assertFalse(Files.exists(gfs.getPath("/some_dir/test_file.txt")));
  }


}
