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

import static org.junit.Assert.*;

public class MoveFileTest extends AbstractParallelGitTest {

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
  public void moveFileWithinSameFileSystem() throws IOException {
    try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", repo)) {             // open git file system
      Path source = gfs.getPath("/example.txt");                                         // convert string to nio path
      Path dest = gfs.getPath("/dest_file.txt");                                         // get the path of the dest file
      Files.move(source, dest);                                                          // move file

      // check
      assertTrue(Files.exists(dest));                                                    // the dest file exists
      assertFalse(Files.exists(source));                                                 // the source file does not exist
    }
  }

  @Test
  public void moveFileToDifferentFileSystem() throws IOException {
    Path dest = tmpFolder.newFile().toPath();                                            // declare dest path (default file system, temporary folder)

    try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", repo)) {             // open git file system
      Path source = gfs.getPath("/example.txt");                                         // convert string to nio path
      Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);                     // move file (with replace option)

      // check
      assertTrue(Files.exists(dest));                                                    // the dest file exists
      assertFalse(Files.exists(source));                                                 // the source file does not exist
    }
  }

  @Test
  public void moveDirectoryWithinSameFileSystem() throws IOException {
    try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", repo)) {             // open git file system
      Path dir = gfs.getPath("/dir");                                                    // convert string to nio path
      Path destDir = gfs.getPath("/dest_dir");                                           // get the path of the dest directory
      Files.move(dir, destDir);                                                          // move directory

      // check
      assertTrue(Files.exists(gfs.getPath("/dest_dir/file_in_directory.txt")));          // child file exists in the dest directory
      assertTrue(Files.exists(gfs.getPath("/dest_dir")));                                // the dest directory exists
      assertFalse(Files.exists(gfs.getPath("/dir/file_in_directory.txt")));              // child file does not exist in the source directory
      assertFalse(Files.exists(gfs.getPath("/dir")));                                    // the source directory does not exist
    }
  }

  @Test
  public void copyDirectoryToAnotherGitFileSystem() throws IOException {
    Repository otherRepo = new TestRepository();                                         // set up the other repository

    try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", repo);               // open source git file system
        GitFileSystem otherGfs = Gfs.newFileSystem("master", otherRepo))        // open dest git file system
    {
      Path dir = gfs.getPath("/dir");                                                    // convert string to nio path
      Path destDir = otherGfs.getPath("/dest_dir");                                      // get the path of the dest directory
      Files.move(dir, destDir);                                                          // move directory

      // check
      assertTrue(Files.exists(otherGfs.getPath("/dest_dir/file_in_directory.txt")));     // child file exists in the dest directory
      assertTrue(Files.exists(otherGfs.getPath("/dest_dir")));                           // the dest directory exists
      assertFalse(Files.exists(gfs.getPath("/dir/file_in_directory.txt")));              // child file does not exist in the source directory
      assertFalse(Files.exists(gfs.getPath("/dir")));                                    // the source directory does not exist
    }
  }

}
