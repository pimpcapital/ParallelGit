package usecases;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import com.beijunyi.parallelgit.filesystem.AbstractGitFileSystemTest;
import com.beijunyi.parallelgit.filesystem.GitPath;
import com.beijunyi.parallelgit.utils.BlobUtils;
import com.beijunyi.parallelgit.utils.CacheUtils;
import com.beijunyi.parallelgit.utils.RepositoryUtils;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.util.FileUtils;
import org.junit.Test;

import static com.beijunyi.parallelgit.utils.CacheUtils.addFile;
import static java.nio.file.StandardOpenOption.APPEND;
import static org.eclipse.jgit.lib.Constants.MASTER;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;
import static org.junit.Assert.*;

public class FilesWriteTest extends AbstractGitFileSystemTest {

  @Test
  public void writeExistingFile_shouldOverwriteItsContent() throws IOException {
    initRepository();
    writeToCache("/file.txt", "old content");
    commitToMaster();
    initGitFileSystem();
    byte[] data =someBytes();
    Path file = gfs.getPath("/file.txt");
    Files.write(file, data);
    assertArrayEquals(data, Files.readAllBytes(file));
  }

  @Test
  public void writeLargeFile_shouldWork() throws IOException {
    repoDir = FileUtils.createTempDir(getClass().getSimpleName(), null, null);
    repo = RepositoryUtils.createRepository(repoDir, true);
    DirCacheBuilder builder = CacheUtils.keepEverything(cache);
    byte[] largeData = new byte[50*1024*1024+1];
    Random random = new Random();
    random.nextBytes(largeData);
    AnyObjectId blobId = BlobUtils.insertBlob(largeData, repo);
    addFile("large.txt", REGULAR_FILE, blobId, builder);
    builder.finish();
    commitToMaster();
    initGitFileSystemForBranch(MASTER);
    byte[] data = someBytes();
    Path file = gfs.getPath("/large.txt");
    Files.write(file, data, APPEND);
  }

  @Test
  public void writeNonExistentFile_shouldCreateNewFile() throws IOException {
    initGitFileSystem();
    GitPath file = gfs.getPath("/file.txt");
    Files.write(file, someBytes());
    assertTrue(Files.exists(file));
  }

  @Test(expected = AccessDeniedException.class)
  public void writeDirectory_shouldThrowAccessDeniedException() throws IOException {
    initRepository();
    writeToCache("/dir/file.txt");
    commitToMaster();
    initGitFileSystem();
    GitPath dir = gfs.getPath("/dir");
    Files.write(dir, someBytes());
  }

  @Test(expected = AccessDeniedException.class)
  public void writeRoot_shouldThrowAccessDeniedException() throws IOException {
    initGitFileSystem();
    Files.write(root, someBytes());
  }




}
