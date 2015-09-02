package usecase;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import com.beijunyi.parallelgit.runtime.ParallelCacheCommand;
import com.beijunyi.parallelgit.utils.CacheHelper;
import com.beijunyi.parallelgit.utils.ObjectUtils;
import com.beijunyi.parallelgit.utils.RevTreeHelper;
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
    AnyObjectId treeWithTwoChildren = RevTreeHelper.getRootTree(repo, commitToMaster());
    DirCache cache = ParallelCacheCommand.prepare(repo)
                       .addDirectory("/dir/", treeWithTwoChildren.getName())
                       .call();
    Assert.assertNotNull(CacheHelper.getEntry(cache, "/dir/file.txt"));
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
    Assert.assertNull(CacheHelper.getEntry(cache, "/dir/file.txt"));
  }

  @Test
  public void deleteFileFollowedByAddingAnotherFile() throws IOException {
    writeToCache("/file.txt");
    AnyObjectId blobId = ObjectUtils.calculateBlobId("some content");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .deleteFile("/file.txt")
                       .addFile("/another_file.txt", blobId)
                       .call();
    Assert.assertNull(CacheHelper.getEntry(cache, "/file.txt"));
    Assert.assertNotNull(CacheHelper.getEntry(cache, "/another_file.txt"));
  }

  @Test
  public void addFileFollowedByDeletingAnotherFile() throws IOException {
    writeToCache("/another_file.txt");
    AnyObjectId blobId = ObjectUtils.calculateBlobId("some content");
    AnyObjectId commitId = commitToMaster();
    DirCache cache = ParallelCacheCommand
                       .prepare(repo)
                       .baseCommit(commitId)
                       .addFile("/file.txt", blobId)
                       .deleteFile("/another_file.txt")
                       .call();
    Assert.assertNotNull(CacheHelper.getEntry(cache, "/file.txt"));
    Assert.assertNull(CacheHelper.getEntry(cache, "/another_file.txt"));
  }

}
