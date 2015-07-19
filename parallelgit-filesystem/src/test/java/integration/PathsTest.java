package integration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
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

}
