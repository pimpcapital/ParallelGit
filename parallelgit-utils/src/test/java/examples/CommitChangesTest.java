package examples;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.*;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommitChangesTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    writeSomethingToCache();
    commitToBranch("my_branch");
  }

  @Test
  public void commitCachedChanges() throws IOException {
    AnyObjectId head = repo.resolve("my_branch");                              // get the head commit of "my_branch"
    DirCache cache = CacheUtils.forRevision(head, repo);                       // load the commit into cache

    byte[] fileContent = "This is an example".getBytes();                      // prepare file content
    AnyObjectId blob = ObjectUtils.insertBlob(fileContent, repo);              // insert file content as blob
    CacheUtils.addFile("/my_file.txt", blob, cache);                           // add the blob id to cache

    AnyObjectId newCommit                                                      // create commit
      = CommitUtils.createCommit("new commit", cache, head, repo);
    BranchUtils.newCommit("my_branch", newCommit, repo);                       // update the head of "my_branch"

    //check
    assertEquals(newCommit, CommitUtils.getCommit("my_branch", repo));         // the branch head equals to the new commit
    assertEquals("This is an example",                                         // the data is correct
                  new String(GitFileUtils.readFile("/my_file.txt", newCommit, repo).getBytes()));
  }

}
