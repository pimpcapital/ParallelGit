package usecases;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.utils.GitFileUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommitEmptyDirectoryTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void commitEmptyDirectory_theEmptyDirectoryShouldNotExistInTheResultCommit() throws IOException {
    writeSomethingToGfs();
    Path dir = gfs.getPath("/empty_dir");
    Files.createDirectory(dir);
    RevCommit commit = Gfs.commit(gfs).execute().getCommit();
    assertFalse(GitFileUtils.exists("/empty_dir", commit, repo));
  }

  @Test
  public void commitEmptyDirectories_theEmptyDirectoriesShouldNotExistInTheResultCommit() throws IOException {
    writeSomethingToGfs();
    Path dir = gfs.getPath("/dir1/dir2");
    Files.createDirectories(dir);
    RevCommit commit = Gfs.commit(gfs).execute().getCommit();
    assertFalse(GitFileUtils.exists("/dir1", commit, repo));
  }

  @Test
  public void commitEmptyDirectory_theEmptyDirectoryShouldExistInTheFileSystemAfterTheOperation() throws IOException {
    writeSomethingToGfs();
    Path dir = gfs.getPath("/empty_dir");
    Files.createDirectory(dir);
    Gfs.commit(gfs).execute();
    assertTrue(Files.exists(dir));
  }



}
