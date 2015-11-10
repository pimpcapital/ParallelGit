package examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.requests.Requests;
import com.beijunyi.parallelgit.filesystem.utils.GitFileSystems;
import com.beijunyi.parallelgit.utils.GitFileUtils;
import com.beijunyi.parallelgit.utils.ObjectUtils;
import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.lib.AnyObjectId;
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
    try(GitFileSystem gfs = GitFileSystems.forRevision("my_branch", repo)) {             // open git file system
      Path file = gfs.getPath("/my_file.txt");                                           // convert string to nio path
      Files.write(file, "my text data".getBytes());                                      // write file
      commit = Requests.commit(gfs).message("my commit message").execute();              // commit changes
    }

    // check
    assertNotNull(commit);                                                               // new commit is created
    assertTrue(GitFileUtils.exists("/my_file.txt", commit, repo));                       // the file exists in the commit
    assertEquals("my text data",                                                         // the data is correct
                  new String(GitFileUtils.readFile("/my_file.txt", commit, repo)));
  }

  @Test
  public void persistBlobs() throws IOException {
    AnyObjectId tree;
    try(GitFileSystem gfs = GitFileSystems.forRevision("my_branch", repo)) {             // open git file system
      Path file = gfs.getPath("/my_file.txt");                                           // convert string to nio path
      Files.write(file, "my text data".getBytes());                                      // write file
      tree = Requests.persist(gfs).execute();                                            // persistRoot changes and create a tree
      assert tree != null;
    }

    // check
    assertTrue(TreeUtils.exists("/my_file.txt", tree, repo));                            // the file exists in the tree
    AnyObjectId blobId = TreeUtils.getObjectId("/my_file.txt", tree, repo);              // get the blob of the file
    assertNotNull(blobId);                                                               // the blob of the file is found
    assertEquals("my text data", new String(ObjectUtils.readObject(blobId, repo)));      // the data is correct
  }

}
