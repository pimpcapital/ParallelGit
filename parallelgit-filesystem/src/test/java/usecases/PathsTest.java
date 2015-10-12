package usecases;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.utils.GitUriBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PathsTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void getRootFromUri() {
    Assert.assertEquals(root, Paths.get(root.toUri()));
  }

  @Test
  public void getArbitraryFileFromUri() {
    Path expected = gfs.getPath("/test_file.txt");
    Assert.assertEquals(expected, Paths.get(expected.toUri()));
  }

  @Test(expected = FileSystemNotFoundException.class)
  public void getArbitraryFileFromUri_invalidSid() {
    URI uri = GitUriBuilder.fromFileSystem(gfs)
                .file("/test_file.txt")
                .sid("some_invalid_sid").build();
    Paths.get(uri);
  }

}
