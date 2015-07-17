package integration;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GitParams;
import com.beijunyi.parallelgit.filesystem.utils.GitUriBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileSystemsTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initFileRepository(true);
  }

  @Test
  public void newFileSystemFromUri() throws IOException {
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, GitParams.emptyMap());
    Assert.assertTrue(fs instanceof GitFileSystem);
    Assert.assertEquals(repoDir, ((GitFileSystem) fs).getRepository().getDirectory());
  }

  @Test
  public void newFileSystemFromUriBranchParam() throws IOException {
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, GitParams.emptyMap().setBranch("test_branch"));
    Assert.assertEquals("test_branch", ((GitFileSystem)fs).getBranch());
  }

  @Test
  public void newFileSystemFromPath() throws IOException {
  }

}
