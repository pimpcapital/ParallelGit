package usecases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GfsTreeWalkTest extends AbstractGitFileSystemTest {

  @Before
  public void setUp() throws IOException {
    initRepository();
  }

  @Test
  public void walkGfsWhenThereAreOnlyRootLevelFiles_theFilesShouldBeIteratedInAlphabeticalOrder() throws IOException {
    String[] files = {
      "/a.txt",
      "/b.txt"
    };
    initGitFileSystem(files);
    assertWalk(false, files);
  }

  @Test
  public void walkGfsWhenThereAreFilesInRootLevelDirectory_theDirectoryShouldBeInTheResult() throws IOException {
    String[] files = {
      "/dir/a.txt",
      "/dir/b.txt"
    };
    initGitFileSystem(files);
    assertWalk(false, "/dir");
  }

  @Test
  public void walkGfsRecursivelyWhenThereAreFilesInDirectories_allTheFilesShouldBeIteratedInAlphabeticalOrder() throws IOException {
    String[] files = {
      "/dir1/a.txt",
      "/dir1/b.txt",
      "/dir2/c.txt",
      "/some_other_file.txt"
    };
    initGitFileSystem(files);
    assertWalk(true, files);
  }


  @Nonnull
  private TreeWalk prepareTreeWalk(boolean recursive) throws IOException {
    GfsTreeIterator iterator = new GfsTreeIterator(gfs);
    TreeWalk ret = new TreeWalk(repo);
    ret.setRecursive(recursive);
    ret.addTree(iterator);
    return ret;
  }

  private void assertWalk(boolean recursive, @Nonnull String... expected) throws IOException {
    try(TreeWalk tw = prepareTreeWalk(recursive)) {
      assertWalk(tw, expected);
    }
  }

  private void assertWalk(@Nonnull TreeWalk tw, @Nonnull String... expected) throws IOException {
    Assert.assertArrayEquals(expected, toArrayWithLeadingSlash(tw));
  }

  @Nonnull
  private String[] toArrayWithLeadingSlash(@Nonnull TreeWalk tw) throws IOException {
    List<String> list = new ArrayList<>();
    while(tw.next())
      list.add("/" + tw.getPathString());
    String[] ret = new String[list.size()];
    return list.toArray(ret);
  }

}
