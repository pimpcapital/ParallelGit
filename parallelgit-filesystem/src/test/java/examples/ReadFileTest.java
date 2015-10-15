package examples;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReadFileTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    writeToCache("/example.txt", "This is an example");
    commitToBranch("my_branch");
  }

  @Test
  public void readBytes() throws IOException {
    try(GitFileSystem gfs = GitFileSystemBuilder.forRevision("my_branch", repo)) {       // open git file system
      Path exampleFile = gfs.getPath("/example.txt");                                    // convert string to nio path
      String fileContent = new String(Files.readAllBytes(exampleFile));                  // read file

      // check
      assertEquals("This is an example", fileContent);                                   // the data is correct
    }
  }

  @Test
  public void openInputStream() throws IOException {
    try(GitFileSystem gfs = GitFileSystemBuilder.forRevision("my_branch", repo)) {       // open git file system
      Path exampleFile = gfs.getPath("/example.txt");                                    // convert string to nio path
      try(InputStream inputStream = Files.newInputStream(exampleFile)) {                 // open input stream
        byte[] bytes = new byte[inputStream.available()];                                // prepare buffer
        assert inputStream.read(bytes) > 0;                                              // read into buffer
        String fileContent = new String(bytes);                                          // convert to string

        // check
        assertEquals("This is an example", fileContent);                                 // the data is correct
      }
    }
  }



}
