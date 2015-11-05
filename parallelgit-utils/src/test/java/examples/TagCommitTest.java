package examples;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.utils.CommitUtils;
import com.beijunyi.parallelgit.utils.TagUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TagCommitTest extends AbstractParallelGitTest {

  @Before
  public void prepareExample() throws IOException {
    initRepository();
    writeSomeFileToCache();
    commitToBranch("my_branch");
  }

  @Test
  public void tagBranchHeadCommit() throws IOException {
    TagUtils.tagCommit("my_tag", "my_branch", repo);                           // tag branch head

    // check
    assertTrue(TagUtils.tagExists("my_tag", repo));                            // "my_tag" exists
    assertEquals(CommitUtils.getCommit("my_branch", repo),                     // the tagged commit equals to the branch head
                  TagUtils.getTaggedCommit("my_tag", repo));
  }

  @Test
  public void tagArbitraryCommit() throws IOException {
    AnyObjectId commit = repo.resolve("my_branch");                            // get the head commit of "my_branch"
    TagUtils.tagCommit("my_tag", commit, repo);                                // tag commit

    // check
    assertTrue(TagUtils.tagExists("my_tag", repo));                            // "my_tag" exists
    assertEquals(commit, TagUtils.getTaggedCommit("my_tag", repo));            // the tagged commit equals to the input commit
  }

}
