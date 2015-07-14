package integration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;

import com.beijunyi.parallelgit.commands.ParallelCommitCommand;
import com.beijunyi.parallelgit.filesystems.GitUriBuilder;
import com.beijunyi.parallelgit.filesystems.GitUriParams;
import com.beijunyi.parallelgit.utils.RepositoryHelper;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.junit.*;

public class FileSystemsTest {

  private File repoDir;
  private Repository repo;

  @Before
  public void setupRepository() throws IOException {
    repoDir = FileUtils.createTempDir(getClass().getSimpleName(), null, null);
    repo = RepositoryHelper.createRepository(repoDir, true);
  }

  @After
  public void disposeRepository() throws IOException {
    repo.close();
    FileUtils.delete(repoDir, FileUtils.RECURSIVE);
  }

  @Test
  public void newFileSystemFromUri() throws IOException {
    byte[] content = "testcontent".getBytes();
    String file = "file.txt";
    ParallelCommitCommand.prepare(repo)
      .master()
      .addFile(content, file)
      .call();
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .master()
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
    Assert.assertArrayEquals(content, Files.readAllBytes(fs.getPath(file)));
  }

  @Test
  public void newFileSystemFromUriWithProperties() throws IOException {
    byte[] content = "testcontent".getBytes();
    String file = "file.txt";
    String branch = "testbranch";
    ParallelCommitCommand.prepare(repo)
      .branch(branch)
      .addFile(content, file)
      .call();
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, Collections.singletonMap(GitUriParams.BRANCH_KEY, branch));
    Assert.assertArrayEquals(content, Files.readAllBytes(fs.getPath(file)));
  }

  @Test
  public void newFileSystemFromPath() throws IOException {
  }

}
