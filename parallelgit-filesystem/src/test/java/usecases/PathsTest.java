package usecases;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.utils.GfsUriBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PathsTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void getRootFromUri() {
    assertEquals(root, Paths.get(root.toUri()));
  }

  @Test
  public void getArbitraryFileFromUri() {
    Path expected = gfs.getPath("/test_file.txt");
    assertEquals(expected, Paths.get(expected.toUri()));
  }

  @Test(expected = FileSystemNotFoundException.class)
  public void getArbitraryFileFromUri_invalidSid() {
    URI uri = GfsUriBuilder.fromFileSystem(gfs)
                .file("/test_file.txt")
                .sid("some_invalid_sid").build();
    Paths.get(uri);
  }

}
