package usecases;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FilesIsWritableTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initRepository();
    writeToCache("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
  }

  @Test
  public void fileIsWritable() {
    assertTrue(Files.isWritable(gfs.getPath("/dir/file.txt")));
  }

  @Test
  public void directoryIsWritable() {
    assertTrue(Files.isWritable(gfs.getPath("/dir")));
  }

  @Test
  public void rootIsWritable() {
    assertTrue(Files.isWritable(gfs.getPath("/")));
  }

  @Test
  public void nonExistentFileIsWritable() {
    assertFalse(Files.isWritable(gfs.getPath("/non_existent_file.txt")));
  }

}
