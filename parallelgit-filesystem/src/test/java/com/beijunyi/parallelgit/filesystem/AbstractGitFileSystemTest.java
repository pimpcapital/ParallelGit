package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;

public abstract class AbstractGitFileSystemTest extends AbstractParallelGitTest {

  protected final GitFileSystemProvider provider = GitFileSystemProvider.getInstance();
  protected GitFileSystem gfs;
  protected GitPath root;

  protected void writeToGfs(@Nonnull String path, @Nonnull byte[] data) throws IOException {
    GitPath file = gfs.getPath(path);
    GitPath parent = file.getParent();
    if(parent != null)
      Files.createDirectories(file.getParent());
    Files.write(file, data);
  }

  protected void writeToGfs(@Nonnull String path, @Nonnull String content) throws IOException {
    writeToGfs(path, Constants.encode(content));
  }

  protected void writeToGfs(@Nonnull String path) throws IOException {
    writeToGfs(path, path + "'s unique content");
  }

  protected void writeSomeFileToGfs() throws IOException {
    writeToGfs("some_file.txt");
  }


  protected void initGitFileSystemForBranch(@Nonnull String branch) throws IOException {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(Gfs.newFileSystem(repo)
                            .branch(branch)
                            .build());
  }

  protected void initGitFileSystemForRevision(@Nonnull AnyObjectId revisionId) throws IOException {
    assert repo != null;
    if(gfs == null)
      injectGitFileSystem(Gfs.newFileSystem(repo)
                            .commit(revisionId)
                            .build());
  }

  protected void initGitFileSystem(@Nonnull String... files) throws IOException {
    if(repo == null)
      initRepository();
    if(files.length != 0) {
      for(String file : files)
        writeToCache(file);
      commitToMaster();
    }
    initGitFileSystemForBranch(Constants.MASTER);
  }

  protected void injectGitFileSystem(@Nonnull GitFileSystem gfs) {
    this.gfs = gfs;
    root = gfs.getRootPath();
  }

}
