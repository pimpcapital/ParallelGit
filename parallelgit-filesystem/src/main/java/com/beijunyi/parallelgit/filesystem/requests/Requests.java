package com.beijunyi.parallelgit.filesystem.requests;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public final class Requests {

  @Nonnull
  public static CommitRequest commit(@Nonnull GitFileSystem gfs) {
    return CommitRequest.prepare(gfs);
  }

  @Nonnull
  public static PersistRequest persist(@Nonnull GitFileSystem gfs) {
    return PersistRequest.prepare(gfs);
  }

  public void copyAndCommitFile(Path src, String dest, Repository repo) throws Exception {
    new Git(repo).checkout().setName("master").call();
    Path destFile = repo.getWorkTree().toPath().resolve(dest);
    Files.copy(src, destFile);
    new Git(repo).commit().setMessage("copied " + src).call();
  }

}
