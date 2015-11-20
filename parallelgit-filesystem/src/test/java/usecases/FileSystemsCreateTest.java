package usecases;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GfsParams;
import com.beijunyi.parallelgit.filesystem.utils.GitUriBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileSystemsCreateTest extends AbstractParallelGitTest {

  @Before
  public void setupRepositoryDirectory() throws IOException {
    initRepositoryDir();
  }

  @Test
  public void createRepository() throws IOException {
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, GfsParams.emptyMap().setCreate(true));
    assertTrue(fs instanceof GitFileSystem);
  }

  @Test(expected = IllegalStateException.class)
  public void createRepository_whenOneAlreadyExists() throws IOException {
    initFileRepository(true);
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystems.newFileSystem(uri, GfsParams.emptyMap().setCreate(true));
  }

  @Test
  public void createBareRepository() throws IOException {
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, GfsParams.emptyMap().setCreate(true).setBare(true));
    assertEquals(repoDir, ((GitFileSystem)fs).getRepository().getDirectory());
  }

  @Test
  public void createNonBareRepository() throws IOException {
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, GfsParams.emptyMap().setCreate(true).setBare(false));
    assertEquals(repoDir, ((GitFileSystem)fs).getRepository().getWorkTree());
  }

}
