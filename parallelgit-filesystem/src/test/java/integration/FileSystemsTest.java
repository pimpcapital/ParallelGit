package integration;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.utils.GitParams;
import com.beijunyi.parallelgit.filesystem.utils.GitUriBuilder;
import com.beijunyi.parallelgit.utils.RevTreeHelper;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileSystemsTest extends AbstractParallelGitTest {

  private AnyObjectId head;

  @Before
  public void setupRepository() throws IOException {
    head = initFileRepository(true);
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
  public void newFileSystemFromUri_withBranchParam() throws IOException {
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, GitParams.emptyMap().setBranch("test_branch"));
    Assert.assertEquals("test_branch", ((GitFileSystem)fs).getBranch());
  }

  @Test
  public void newFileSystemFromUri_withRevisionParam() throws IOException {
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    FileSystem fs = FileSystems.newFileSystem(uri, GitParams.emptyMap().setRevision(head));
    Assert.assertEquals(head, ((GitFileSystem)fs).getCommit());
  }

  @Test
  public void newFileSystemFromUri_withTreeParam() throws IOException {
    URI uri = GitUriBuilder.prepare()
                .repository(repoDir)
                .build();
    AnyObjectId treeId = RevTreeHelper.getRootTree(repo, head);
    FileSystem fs = FileSystems.newFileSystem(uri, GitParams.emptyMap().setTree(treeId));
    Assert.assertEquals(treeId, ((GitFileSystem)fs).getTree());
  }


  @Test
  public void newFileSystemFromPath() throws IOException {
    FileSystem fs = FileSystems.newFileSystem(repoDir.toPath(), null);
    Assert.assertTrue(fs instanceof GitFileSystem);
    Assert.assertEquals(repoDir, ((GitFileSystem) fs).getRepository().getDirectory());
  }

}
