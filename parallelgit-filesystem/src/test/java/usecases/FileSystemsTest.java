package usecases;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GfsUriBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static com.beijunyi.parallelgit.filesystem.GitFileSystemProvider.*;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;

public class FileSystemsTest extends AbstractParallelGitTest {

  private RevCommit head;

  @Before
  public void setupRepository() throws IOException {
    head = initFileRepository(true);
  }

  @Test
  public void newFileSystemFromUri_theResultFileSystemShouldBaseOnTheSpecifiedPath() throws IOException {
    URI uri = GfsUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String, String>emptyMap());
    assertTrue(fs instanceof GitFileSystem);
    assertEquals(repoDir, ((GitFileSystem)fs).getRepository().getDirectory());
  }

  @Test
  public void newFileSystemFromUriWithBranchParam_theResultFileSystemShouldBeAttachedToTheSpecifiedBranch() throws IOException {
    URI uri = GfsUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, singletonMap(BRANCH, "test_branch"));
    assertEquals("test_branch", ((GitFileSystem)fs).getStatusProvider().branch());
  }

  @Test
  public void newFileSystemFromUriWithCommitParam_theResultFileSystemHeadCommitShouldBeTheSpecifiedCommit() throws IOException {
    URI uri = GfsUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, singletonMap(COMMIT, head.name()));
    assertEquals(head, ((GitFileSystem)fs).getStatusProvider().commit());
  }

  @Test
  public void newFileSystemFromPath_theResultFileSystemShouldBaseOnTheSpecifiedPath() throws IOException {
    FileSystem fs = FileSystems.newFileSystem(repoDir.toPath(), null);
    assertTrue(fs instanceof GitFileSystem);
    assertEquals(repoDir, ((GitFileSystem)fs).getRepository().getDirectory());
  }

}
