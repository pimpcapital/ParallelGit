package usecases;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.requests.Requests;
import com.beijunyi.parallelgit.utils.GitFileUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

public class CommitEmptyDirectoryTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void commitEmptyDirectory_theEmptyDirectoryShouldNotExistInTheResultCommit() throws IOException {
    writeSomeFileToGfs();
    Path dir = gfs.getPath("/empty_dir");
    Files.createDirectory(dir);
    RevCommit commit = Requests.commit(gfs).execute();
    assert commit != null;
    GitFileUtils.exists("/empty_dir", commit, repo);
  }

  @Test
  public void commitEmptyDirectories_theEmptyDirectoriesShouldNotExistInTheResultCommit() throws IOException {
    writeSomeFileToGfs();
    Path dir = gfs.getPath("/dir1/dir2");
    Files.createDirectories(dir);
    RevCommit commit = Requests.commit(gfs).execute();
    assert commit != null;
    GitFileUtils.exists("/empty1", commit, repo);
  }

}
