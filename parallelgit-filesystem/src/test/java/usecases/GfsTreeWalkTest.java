package usecases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator;
import com.beijunyi.parallelgit.utils.TreeUtils;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.junit.Before;
import org.junit.Test;

import static org.eclipse.jgit.lib.FileMode.*;
import static org.junit.Assert.*;

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

  @Test
  public void getFileModeFromFileNode_shouldEqualRegularFile() throws IOException {
    initGitFileSystem("/test_file.txt");
    TreeWalk tw = forPath("/test_file.txt");
    assertEquals(REGULAR_FILE, tw.getFileMode(0));
  }

  @Test
  public void getFileModeFromDirectoryNode_shouldEqualTree() throws IOException {
    initGitFileSystem("/dir/some_file.txt");
    TreeWalk tw = forPath("/dir");
    assertEquals(TREE, tw.getFileMode(0));
    assertTrue(TreeUtils.isDirectory(tw));
  }

  @Test
  public void getObjectIdFromFileNode_shouldReturnTheBlobIdOfTheFile() throws IOException {
    initRepository();
    byte[] data = someBytes();
    writeToCache("/test_file.txt", data);
    commitToMaster();
    initGitFileSystem();

    TreeWalk tw = forPath("/test_file.txt");
    assertEquals(calculateBlobId(data), TreeUtils.getObjectId(tw));
  }


  @Nonnull
  private TreeWalk prepareTreeWalk(boolean recursive) throws IOException {
    GfsTreeIterator iterator = GfsTreeIterator.iterateRoot(gfs);
    TreeWalk ret = new TreeWalk(repo);
    ret.setRecursive(recursive);
    ret.addTree(iterator);
    return ret;
  }

  private void assertWalk(boolean recursive, String... expected) throws IOException {
    try(TreeWalk tw = prepareTreeWalk(recursive)) {
      assertWalk(tw, expected);
    }
  }

  private void assertWalk(TreeWalk tw, String... expected) throws IOException {
    assertArrayEquals(expected, toArrayWithLeadingSlash(tw));
  }

  @Nonnull
  private String[] toArrayWithLeadingSlash(TreeWalk tw) throws IOException {
    List<String> list = new ArrayList<>();
    while(tw.next())
      list.add("/" + tw.getPathString());
    String[] ret = new String[list.size()];
    return list.toArray(ret);
  }

  @Nonnull
  private TreeWalk forPath(String path) throws IOException {
    TreeWalk tw = prepareTreeWalk(false);
    PathFilter filter = PathFilter.create(path.charAt(0) == '/' ? path.substring(1) : path);
    tw.setFilter(filter);
    tw.setRecursive(false);
    while(tw.next()) {
      if(filter.isDone(tw))
        return tw;
      if(tw.isSubtree())
        tw.enterSubtree();
    }
    throw new IllegalStateException();
  }

}
