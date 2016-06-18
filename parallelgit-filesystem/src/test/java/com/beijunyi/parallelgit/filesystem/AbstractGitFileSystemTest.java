package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.junit.After;

import static java.nio.file.Files.readAllBytes;
import static org.eclipse.jgit.lib.Constants.*;
import static org.eclipse.jgit.util.RawParseUtils.decode;

public abstract class AbstractGitFileSystemTest extends AbstractParallelGitTest {

  protected final GitFileSystemProvider provider = GitFileSystemProvider.getDefault();

  protected GitFileSystem gfs;
  protected GfsStatusProvider status;
  protected GfsObjectService objService;
  protected GitPath root;

  @After
  public void closeFileSystem() throws IOException {
    if(gfs != null) {
      gfs.close();
      gfs = null;
    }
  }

  protected void writeToGfs(String path, byte[] data) throws IOException {
    GitPath file = gfs.getPath(path);
    GitPath parent = file.getParent();
    if(parent != null)
      Files.createDirectories(file.getParent());
    Files.write(file, data);
  }

  protected void writeToGfs(String path, String content) throws IOException {
    writeToGfs(path, encode(content));
  }

  protected void writeToGfs(String path) throws IOException {
    writeToGfs(path, path + "'s unique content");
  }

  protected void writeSomethingToGfs() throws IOException {
    writeToGfs("some_file.txt");
  }

  protected void initGitFileSystemForBranch(String branch) throws IOException {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(Gfs.newFileSystem(branch, repo));
  }

  protected void initGitFileSystem(String... files) throws IOException {
    if(repo == null)
      initRepository();
    if(files.length != 0) {
      writeMultipleToCache(files);
      commitToMaster();
    }
    initGitFileSystemForBranch(MASTER);
  }

  protected void injectGitFileSystem(GitFileSystem gfs) {
    this.gfs = gfs;
    root = gfs.getRootPath();
    status = gfs.getStatusProvider();
    objService = gfs.getObjectService();
  }

  @Nonnull
  protected static String readAsString(Path path) throws IOException {
    return decode(readAllBytes(path));
  }

}
