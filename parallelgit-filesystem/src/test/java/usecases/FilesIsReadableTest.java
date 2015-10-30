package usecases;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FilesIsReadableTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initRepository();
    writeToCache("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
  }

  @Test
  public void fileIsReadable() {
    assertTrue(Files.isReadable(gfs.getPath("/dir/file.txt")));
  }

  @Test
  public void directoryIsReadable() {
    assertTrue(Files.isReadable(gfs.getPath("/dir")));
  }

  @Test
  public void rootIsReadable() {
    assertTrue(Files.isReadable(gfs.getPath("/")));
  }

  @Test
  public void nonExistentFileIsReadable() {
    assertFalse(Files.isReadable(gfs.getPath("/non_existent_file.txt")));
  }

}
