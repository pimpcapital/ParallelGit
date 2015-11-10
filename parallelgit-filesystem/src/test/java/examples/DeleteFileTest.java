package examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystems;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class DeleteFileTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    writeToCache("/example.txt", "This is an example");
    writeToCache("/dir/file_in_directory.txt", "This is another example");
    commitToBranch("my_branch");
  }

  @Test
  public void deleteFile() throws IOException {
    try(GitFileSystem gfs = GitFileSystems.forRevision("my_branch", repo)) {             // open git file system
      Path exampleFile = gfs.getPath("/example.txt");                                    // convert string to nio path
      Files.delete(exampleFile);                                                         // delete file

      // check
      assertFalse(Files.exists(exampleFile));                                            // file is deleted
    }
  }

  @Test
  public void deleteDirectory() throws IOException {
    try(GitFileSystem gfs = GitFileSystems.forRevision("my_branch", repo)) {             // open git file system
      Path dir = gfs.getPath("/dir");                                                    // convert string to nio path
      Files.delete(dir);                                                                 // delete directory

      // check
      assertFalse(Files.exists(gfs.getPath("/dir/file_in_directory.txt")));              // child file is deleted
      assertFalse(Files.exists(gfs.getPath("/dir")));                                    // directory is deleted
    }
  }



}
