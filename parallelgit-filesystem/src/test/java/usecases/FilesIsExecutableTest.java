package usecases;

import java.io.IOException;
import java.nio.file.Files;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FilesIsExecutableTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initRepository();
    writeToCache("dir/file.txt");
    commitToMaster();
    initGitFileSystem();
  }

  @Test
  public void fileIsExecutable() {
    Assert.assertFalse(Files.isExecutable(gfs.getPath("/dir/file.txt")));
  }

  @Test
  public void directoryIsExecutable() {
    Assert.assertFalse(Files.isExecutable(gfs.getPath("/dir")));
  }

  @Test
  public void rootIsExecutable() {
    Assert.assertFalse(Files.isExecutable(gfs.getPath("/")));
  }

  @Test
  public void nonExistentFileIsExecutable() {
    Assert.assertFalse(Files.isExecutable(gfs.getPath("/non_existent_file.txt")));
  }

}
