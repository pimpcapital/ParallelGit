package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.attribute.*;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.hierarchy.RootNode;

public abstract class GitFileStoreAttributeView implements FileStoreAttributeView {

  protected final RootNode root;

  protected GitFileStoreAttributeView(@Nonnull RootNode root) {
    this.root = root;
  }

  public static class Basic extends GitFileStoreAttributeView implements BasicFileAttributeView {

    private BasicFileAttributeView rootView;

    public Basic(@Nonnull RootNode root) {
      super(root);
    }

    @Override
    public String name() {
      return GitFileAttributeView.Basic.BASIC_VIEW;
    }

    @Override
    public BasicFileAttributes readAttributes() throws IOException {

      return null;
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {

    }
  }

}
