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

public class ReadFile extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initMemoryRepository(true);
    writeToCache("/example.txt", "This is an example");
    commitToBranch("my_branch");
  }

  @Test
  public void readFile() throws IOException {
    try(GitFileSystem gfs = GitFileSystemBuilder.forRevision("my_branch", repo)) {        // open git file system
      Path exampleFile = gfs.getPath("/example.txt");                                     // convert string to nio path
      String fileContent = new String(Files.readAllBytes(exampleFile));                   // read file

      // check
      Assert.assertEquals("This is an example", fileContent);                             // the data is correct
    }
  }



}
