package examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

public class CopyFile extends AbstractParallelGitTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Before
  public void prepareExample() throws IOException {
    initMemoryRepository(true);
    writeToCache("/example.txt", "This is an example");
    commitToBranch("my_branch");
  }

  @Test
  public void copyFileWithinSameFileSystem() throws IOException {
    try(GitFileSystem gfs = GitFileSystemBuilder.forRevision("my_branch", repo)) {        // open git file system
      Path exampleFile = gfs.getPath("/example.txt");                                     // convert string to nio path
      Path dest = gfs.getPath("/dest_file.txt");                                          // get the path of the dest file
      Files.copy(exampleFile, dest);                                                      // copy file

      // check
      Assert.assertTrue(Files.exists(dest));
    }
  }

  @Test
  public void copyFileToDifferentFileSystem() throws IOException {
    Path dest = tmpFolder.newFile().toPath();

    try(GitFileSystem gfs = GitFileSystemBuilder.forRevision("my_branch", repo)) {        // open git file system
      Path exampleFile = gfs.getPath("/example.txt");                                     // convert string to nio path
      Files.copy(exampleFile, dest, StandardCopyOption.REPLACE_EXISTING);                 // copy file (with replace option)
    }

    // check
    Assert.assertTrue(Files.exists(dest));
  }

}
