package usecases;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.runtime.ParallelCacheCommand;
import com.beijunyi.parallelgit.utils.CacheUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ParallelCacheCommandUseCaseTest extends AbstractParallelGitTest {

  @Before
  public void setupRepository() throws IOException {
    initRepository();
  }

  @Test
  public void addDirectoryWhenInputNameEndsWithSlash() throws IOException {
    writeToCache("/file.txt");
    AnyObjectId treeWithTwoChildren = commitToMaster().getTree();
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .addDirectory("/dir/", treeWithTwoChildren.getName())
                       .call();
    Assert.assertNotNull(CacheUtils.getEntry("/dir/file.txt", cache));
  }

  @Test
  public void deleteDirectoryWhenInputNameEndsWithSlash() throws IOException {
    writeToCache("/dir/file.txt");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .deleteDirectory("/dir/")
                       .call();
    Assert.assertNull(CacheUtils.getEntry("/dir/file.txt", cache));
  }

  @Test
  public void deleteFileFollowedByAddingAnotherFile() throws IOException {
    writeToCache("/file.txt");
    AnyObjectId blobId = someObjectId();
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .deleteFile("/file.txt")
                       .addFile("/another_file.txt", blobId)
                       .call();
    Assert.assertNull(CacheUtils.getEntry("/file.txt", cache));
    Assert.assertNotNull(CacheUtils.getEntry("/another_file.txt", cache));
  }

  @Test
  public void addFileFollowedByDeletingAnotherFile() throws IOException {
    writeToCache("/another_file.txt");
    AnyObjectId blobId = someObjectId();
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .addFile("/file.txt", blobId)
                       .deleteFile("/another_file.txt")
                       .call();
    Assert.assertNotNull(CacheUtils.getEntry("/file.txt", cache));
    Assert.assertNull(CacheUtils.getEntry("/another_file.txt", cache));
  }

}
