package examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.GitFileUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommitChangesTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    commitToBranch("my_branch");
  }

  @Test
  public void commitChanges() throws IOException {
    RevCommit commit;
    try(GitFileSystem gfs = Gfs.newFileSystem("my_branch", repo)) {             // open git file system
      Path file = gfs.getPath("/my_file.txt");                                           // convert string to nio path
      Files.write(file, "my text data".getBytes());                                      // write file
      commit = Gfs.commit(gfs).message("my commit message").execute().getCommit();              // commit changes
    }

    // check
    assertNotNull(commit);                                                               // new commit is created
    assertTrue(GitFileUtils.exists("/my_file.txt", commit, repo));                       // the file exists in the commit
    assertEquals("my text data",                                                         // the data is correct
                  new String(GitFileUtils.readFile("/my_file.txt", commit, repo).getData()));
  }


}
