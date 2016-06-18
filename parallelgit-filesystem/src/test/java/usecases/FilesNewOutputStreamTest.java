package usecases;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.GitPath;
import org.junit.Test;

import static org.junit.Assert.*;

public class FilesNewOutputStreamTest extends AbstractGitFileSystemTest {

  @Test
  public void openNewOutputStream_theResultShouldBeNotNull() throws IOException {
    initRepository();
    writeToCache("/test_file.txt");
    commitToMaster();
    initGitFileSystem();

    try(OutputStream stream = Files.newOutputStream(gfs.getPath("/test_file.txt"))) {
      assertNotNull(stream);
    }
  }

  @Test
  public void openNewOutputStreamWhenFileDoesNotExist_shouldCreateNewFile() throws IOException {
    initGitFileSystem();
    GitPath file = gfs.getPath("/test_file.txt");
    Files.newOutputStream(file);
    assertTrue(Files.exists(file));
  }

  @Test
  public void openNewOutputStreamWhenFileDoesNotExist_theNewFileIsEmptyBeforeStreamCloses() throws IOException {
    initGitFileSystem();
    GitPath file = gfs.getPath("/non_existent_file.txt");
    try(OutputStream stream = Files.newOutputStream(file)) {
      stream.write(someBytes());
      assertEquals(0, Files.size(file));
    }
  }

  @Test
  public void openNewOutputStreamWhenFileDoesNotExist_theNewFileHasTheOutputDataAfterStreamCloses() throws IOException {
    initGitFileSystem();

    byte[] expected = someBytes();
    GitPath file = gfs.getPath("/test_file.txt");
    try(OutputStream stream = Files.newOutputStream(file)) {
      stream.write(expected);
    }
    assertArrayEquals(expected, Files.readAllBytes(file));
  }

}
