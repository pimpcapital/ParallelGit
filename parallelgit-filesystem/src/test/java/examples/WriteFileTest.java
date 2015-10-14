package examples;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystemBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WriteFileTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initMemoryRepository(true);
    writeSomeFileToCache();
    commitToBranch("my_branch");
  }

  @Test
  public void writeBytes() throws IOException {
    try(GitFileSystem gfs = GitFileSystemBuilder.forRevision("my_branch", repo)) {        // open git file system
      Path file = gfs.getPath("/my_file.txt");                                            // convert string to nio path
      Files.write(file, "my text data".getBytes());                                       // write file

      // check
      assertTrue(Files.exists(file));                                                     // the file exists
      assertEquals("my text data", new String(Files.readAllBytes(file)));                 // the data is correct
    }
  }

  @Test
  public void openOutputStream() throws IOException {
    try(GitFileSystem gfs = GitFileSystemBuilder.forRevision("my_branch", repo)) {        // open git file system
      Path file = gfs.getPath("/my_file.txt");                                            // convert string to nio path
      try(OutputStream outputStream = Files.newOutputStream(file)) {                      // open output stream
        outputStream.write("my text data".getBytes());                                    // write data
      }

      // check
      assertTrue(Files.exists(file));                                                     // the file exists
      assertEquals("my text data", new String(Files.readAllBytes(file)));                 // the data is correct
    }
  }



}
