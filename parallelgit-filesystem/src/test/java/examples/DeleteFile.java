package examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DeleteFile extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initMemoryRepository(true);
    writeToCache("/example.txt", "This is an example");
    writeToCache("/dir/another_example.txt", "This is another example");
    commitToBranch("my_branch");
  }

  @Test
  public void deleteFile() throws IOException {
    try(GitFileSystem gfs = GitFileSystemBuilder.forRevision("my_branch", repo)) {        // open git file system
      Path exampleFile = gfs.getPath("/example.txt");                                     // convert string to nio path
      Files.delete(exampleFile);                                                          // delete file

      // check
      Assert.assertFalse(Files.exists(exampleFile));                                      // file is deleted
    }
  }

  @Test
  public void deleteDirectory() throws IOException {
    try(GitFileSystem gfs = GitFileSystemBuilder.forRevision("my_branch", repo)) {        // open git file system
      Path dir = gfs.getPath("/dir");                                                     // convert string to nio path
      Files.delete(dir);                                                                  // delete directory

      // check
      Assert.assertFalse(Files.exists(gfs.getPath("/dir/another_example.txt")));          // child file is deleted
      Assert.assertFalse(Files.exists(gfs.getPath("/dir")));                              // directory is deleted
    }
  }



}
