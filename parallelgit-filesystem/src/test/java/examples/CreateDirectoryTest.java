package examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CreateDirectoryTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    writeSomethingToCache();
    commitToBranch("my_branch");
  }

  @Test
  public void createDirectory() throws IOException {
    try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", repo)) {             // open git file system
      Path dir = gfs.getPath("/new_dir");                                                // convert string to nio path
      Files.createDirectory(dir);                                                        // create directory

      // check
      assertTrue(Files.exists(dir));                                                     // the directory exists
    }
  }

  @Test
  public void createDirectories() throws IOException {
    try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", repo)) {             // open git file system
      Path dir = gfs.getPath("/dir1/dir2");                                              // convert string to nio path
      Files.createDirectories(dir);                                                      // create directories

      // check
      assertTrue(Files.exists(gfs.getPath("/dir1/dir2")));                               // the child directory exists
      assertTrue(Files.exists(gfs.getPath("/dir1")));                                    // the parent directory exists
    }
  }



}
