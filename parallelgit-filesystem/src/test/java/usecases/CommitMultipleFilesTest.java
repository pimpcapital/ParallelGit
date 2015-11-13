package usecases;

import java.io.IOException;

import com.beijunyi.parallelgit.filesystem.Gfs;
import com.beijunyi.parallelgit.filesystem.PreSetupGitFileSystemTest;
import com.beijunyi.parallelgit.utils.GitFileUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CommitMultipleFilesTest extends PreSetupGitFileSystemTest {

  @Test
  public void commitMultipleFiles_allFilesShouldExistInTheResultCommit() throws IOException {
    String[] files = {"/file1.txt", "/file2.txt", "/file3.txt"};
    for(String file : files)
      writeToGfs(file);
    RevCommit commit = Gfs.commit(gfs).execute();
    assert commit != null;
    for(String file : files)
      assertTrue(GitFileUtils.exists(file, commit, repo));
  }

  @Test
  public void commitMultipleFilesInMultipleDirectories_allFilesShouldExistInTheResultCommit() throws IOException {
    String[] files = {"/dir1/file11.txt", "/dir2/dir21/file211.txt", "/dir2/file22.txt", "/dir2/file23.txt"};
    for(String file : files)
      writeToGfs(file);
    RevCommit commit = Gfs.commit(gfs).execute();
    assert commit != null;
    for(String file : files)
      assertTrue(GitFileUtils.exists(file, commit, repo));
  }

}
