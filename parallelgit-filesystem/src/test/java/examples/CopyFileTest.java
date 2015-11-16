package examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

public class CopyFileTest extends AbstractParallelGitTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    writeToCache("/example.txt", "This is an example");
    writeToCache("/dir/file_in_directory.txt", "This is another example");
    commitToBranch("my_branch");
  }

  @Test
  public void copyFileWithinSameFileSystem() throws IOException {
    try(GitFileSystem gfs = Gfs.forRevision("my_branch", repo)) {             // open git file system
      Path exampleFile = gfs.getPath("/example.txt");                                    // convert string to nio path
      Path dest = gfs.getPath("/dest_file.txt");                                         // get the path of the dest file
      Files.copy(exampleFile, dest);                                                     // copy file

      // check
      assertTrue(Files.exists(dest));                                                    // the dest file exists
    }
  }

  @Test
  public void copyFileToDifferentFileSystem() throws IOException {
    Path dest = tmpFolder.newFile().toPath();                                            // declare dest path (default file system, temporary folder)

    try(GitFileSystem gfs = Gfs.forRevision("my_branch", repo)) {             // open git file system
      Path exampleFile = gfs.getPath("/example.txt");                                    // convert string to nio path
      Files.copy(exampleFile, dest, StandardCopyOption.REPLACE_EXISTING);                // copy file (with replace option)
    }

    // check
    assertTrue(Files.exists(dest));                                                      // the dest file exists
  }

  @Test
  public void copyDirectoryWithinSameFileSystem() throws IOException {
    try(GitFileSystem gfs = Gfs.forRevision("my_branch", repo)) {              // open git file system
      Path dir = gfs.getPath("/dir");                                                     // convert string to nio path
      Path destDir = gfs.getPath("/dest_dir");                                            // get the path of the dest directory
      Files.copy(dir, destDir);                                                           // copy directory

      // check
      assertTrue(Files.exists(gfs.getPath("/dest_dir/file_in_directory.txt")));           // child file exists in the dest directory
      assertTrue(Files.exists(gfs.getPath("/dest_dir")));                                 // the dest directory exists
    }
  }

  @Test
  public void copyDirectoryToAnotherFileSystem() throws IOException {
    Repository otherRepo = new TestRepository();                                          // set up the other repository

    try(GitFileSystem gfs = Gfs.forRevision("my_branch", repo);                // open source git file system
        GitFileSystem otherGfs = Gfs.forRevision("master", otherRepo)) {       // open dest git file system
      Path dir = gfs.getPath("/dir");                                                     // convert string to nio path
      Path destDir = otherGfs.getPath("/dest_dir");                                       // get the path of the dest directory
      Files.copy(dir, destDir);                                                           // copy directory

      // check
      assertTrue(Files.exists(otherGfs.getPath("/dest_dir/file_in_directory.txt")));      // child file exists in the dest directory
      assertTrue(Files.exists(otherGfs.getPath("/dest_dir")));                            // the dest directory exists
    }
  }

}
