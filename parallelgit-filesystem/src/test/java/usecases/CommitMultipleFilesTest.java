package usecases;

import java.io.IOException;
import javax.annotation.Nonnull;

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
    RevCommit commit = writeAndCommit(files);
    for(String file : files)
      assertTrue(GitFileUtils.exists(file, commit, repo));
  }

  @Test
  public void commitFilesInDifferentDepthDirectories_allFilesShouldExistInTheResultCommit() throws IOException {
    String[] files = {"/dir1/file11.txt", "/dir2/dir21/file211.txt", "/dir2/file22.txt", "/dir2/file23.txt"};
    RevCommit commit = writeAndCommit(files);
    for(String file : files)
      assertTrue(GitFileUtils.exists(file, commit, repo));
  }

  @Nonnull
  private RevCommit writeAndCommit(String[] files) throws IOException {
    for(String file : files)
      writeToGfs(file);
    return Gfs.commit(gfs).execute().getCommit();
  }

}
