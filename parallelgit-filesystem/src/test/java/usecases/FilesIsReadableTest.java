package usecases;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    Assert.assertTrue(Files.isReadable(gfs.getPath("/dir/file.txt")));
  }

  @Test
  public void directoryIsReadable() {
    Assert.assertTrue(Files.isReadable(gfs.getPath("/dir")));
  }

  @Test
  public void rootIsReadable() {
    Assert.assertTrue(Files.isReadable(gfs.getPath("/")));
  }

  @Test
  public void nonExistentFileIsReadable() {
    Assert.assertFalse(Files.isReadable(gfs.getPath("/non_existent_file.txt")));
  }

}
