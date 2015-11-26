package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.treewalk.TreeWalk;

public class ThreeWayEntry {

  private final String path;
  private final String name;
  private final GitFileEntry base;
  private final GitFileEntry ours;
  private final GitFileEntry theirs;

  private ThreeWayEntry(@Nonnull String path, @Nonnull String name,
                         @Nonnull GitFileEntry base, @Nonnull GitFileEntry ours, @Nonnull GitFileEntry theirs) {
    this.path = path;
    this.name = name;
    this.base = base;
    this.ours = ours;
    this.theirs = theirs;
  }

  @Nonnull
  public static ThreeWayEntry read(@Nonnull TreeWalk tw) {
    return new ThreeWayEntry(tw.getPathString(), tw.getNameString(),
                              new GitFileEntry(tw.getObjectId(0), tw.getFileMode(0)),
                              new GitFileEntry(tw.getObjectId(1), tw.getFileMode(1)),
                              new GitFileEntry(tw.getObjectId(2), tw.getFileMode(2)));
  }

  @Nonnull
  public String getPath() throws IOException {
    return path;
  }

  @Nonnull
  public String getName() throws IOException {
    return name;
  }

  @Nonnull
  public GitFileEntry getBase() throws IOException {
    return base;
  }

  @Nonnull
  public GitFileEntry getOurs() throws IOException {
    return ours;
  }

  @Nonnull
  public GitFileEntry getTheirs() throws IOException {
    return theirs;
  }

}
