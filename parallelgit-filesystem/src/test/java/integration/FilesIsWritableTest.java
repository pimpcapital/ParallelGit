package integration;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FilesIsWritableTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initRepository();
    writeFile("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
  }

  @Test
  public void fileIsWritable() {
    Assert.assertTrue(Files.isWritable(gfs.getPath("/dir/file.txt")));
  }

  @Test
  public void directoryIsWritable() {
    Assert.assertTrue(Files.isWritable(gfs.getPath("/dir")));
  }

  @Test
  public void rootIsWritable() {
    Assert.assertTrue(Files.isWritable(gfs.getPath("/")));
  }

  @Test
  public void nonExistentFileIsWritable() {
    Assert.assertFalse(Files.isWritable(gfs.getPath("/non_existent_file.txt")));
  }

}
