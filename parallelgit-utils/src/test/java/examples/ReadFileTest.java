package examples;

import java.io.IOException;
import java.io.InputStream;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.GitFileUtils;
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
    String fileContent =                                                       // read file
      new String(GitFileUtils.readFile("/example.txt", "my_branch", repo).getData());

    // check
    assertEquals("This is an example", fileContent);                           // the data is correct
  }

  @Test
  public void openInputStream() throws IOException {
    try(InputStream inputStream                                                // open input stream
          = GitFileUtils.openFile("/example.txt", "my_branch", repo)) {
      byte[] bytes = new byte[inputStream.available()];                        // prepare buffer
      assert inputStream.read(bytes) > 0;                                      // read into buffer
      String fileContent = new String(bytes);                                  // convert to string

      // check
      assertEquals("This is an example", fileContent);                         // the data is correct
    }
  }

}
