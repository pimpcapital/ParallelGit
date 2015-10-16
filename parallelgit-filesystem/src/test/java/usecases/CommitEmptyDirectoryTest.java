package usecases;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.requests.Requests;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

public class CommitEmptyDirectoryTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void commitEmptyDirectory_() throws IOException {
    writeSomeFileToGfs();
    Path dir = gfs.getPath("/empty_dir");
    Files.createDirectory(dir);
    RevCommit commit = Requests.commit(gfs).execute();
  }

}
