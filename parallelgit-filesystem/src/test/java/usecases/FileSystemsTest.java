package usecases;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GfsParams;
import com.beijunyi.parallelgit.filesystem.utils.GfsUriBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileSystemsTest extends AbstractParallelGitTest {

  private RevCommit head;

  @Before
  public void setupRepository() throws IOException {
    head = initFileRepository(true);
  }

  @Test
  public void newFileSystemFromUri() throws IOException {
    URI uri = GfsUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, GfsParams.emptyMap());
    assertTrue(fs instanceof GitFileSystem);
    assertEquals(repoDir, ((GitFileSystem)fs).getRepository().getDirectory());
  }

  @Test
  public void newFileSystemFromUri_withBranchParam() throws IOException {
    URI uri = GfsUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, GfsParams.emptyMap().branch("test_branch"));
    assertEquals("test_branch", ((GitFileSystem)fs).getStatusProvider().branch());
  }

  @Test
  public void newFileSystemFromUri_withRevisionParam() throws IOException {
    URI uri = GfsUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, GfsParams.emptyMap().commit(head));
    assertEquals(head, ((GitFileSystem)fs).getStatusProvider().commit());
  }

  @Test
  public void newFileSystemFromPath() throws IOException {
    FileSystem fs = FileSystems.newFileSystem(repoDir.toPath(), null);
    assertTrue(fs instanceof GitFileSystem);
    assertEquals(repoDir, ((GitFileSystem)fs).getRepository().getDirectory());
  }

}
