package com.beijunyi.parallelgit.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.runtime.cache.*;
import com.beijunyi.parallelgit.utils.CacheUtils;
import com.beijunyi.parallelgit.utils.CommitUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;

public abstract class CacheBasedCommand<B extends CacheBasedCommand, T> extends ParallelCommand<T> {

  protected AnyObjectId baseCommitId;
  protected String baseCommitIdStr;
  protected AnyObjectId baseTreeId;
  protected String baseTreeIdStr;
  protected final List<CacheEditor> editors = new ArrayList<>();
  protected final Repository repository;

  protected CacheBasedCommand(@Nullable Repository repository) {
    this.repository = repository;
  }

  @Nonnull
  protected abstract B self();

  @Nonnull
  public B baseCommit(@Nonnull AnyObjectId commitId) {
    this.baseCommitId = commitId;
    return self();
  }

  @Nonnull
  public B baseCommit(@Nonnull String commitIdStr) {
    this.baseCommitIdStr = commitIdStr;
    return self();
  }

  @Nonnull
  public B baseTree(@Nonnull AnyObjectId treeId) {
    this.baseTreeId = treeId;
    return self();
  }

  @Nonnull
  public B baseTree(@Nonnull String treeIdStr) {
    this.baseTreeIdStr = treeIdStr;
    return self();
  }

  @Nonnull
  public B addDirectory(@Nonnull String path, @Nonnull AnyObjectId treeId) {
    AddTree editor = new AddTree(path);
    editor.setTreeId(treeId);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B addDirectory(@Nonnull String path, @Nonnull String treeIdStr) {
    AddTree editor = new AddTree(path);
    editor.setTreeIdStr(treeIdStr);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B addFile(@Nonnull String path, @Nonnull AnyObjectId blobId, @Nonnull FileMode mode) {
    AddEntry editor = new AddEntry(path);
    editor.setBlobId(blobId);
    editor.setMode(mode);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B addFile(@Nonnull String path, @Nonnull AnyObjectId blobId) {
    return addFile(path, blobId, FileMode.REGULAR_FILE);
  }

  @Nonnull
  public B deleteDirectory(@Nonnull String path) {
    DeleteTree editor = new DeleteTree(path);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B deleteFile(@Nonnull String path) {
    DeleteEntry editor = new DeleteEntry(path);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B updateFile(@Nonnull String path, @Nonnull AnyObjectId blobId, @Nonnull FileMode mode) {
    UpdateEntry editor = new UpdateEntry(path);
    editor.setBlobId(blobId);
    editor.setMode(mode);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B updateFile(@Nonnull String path, @Nonnull AnyObjectId blobId) {
    UpdateEntry editor = new UpdateEntry(path);
    editor.setBlobId(blobId);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B updateFile(@Nonnull String path, @Nonnull FileMode mode) {
    UpdateEntry editor = new UpdateEntry(path);
    editor.setMode(mode);
    editors.add(editor);
    return self();
  }

  protected boolean isBaseSpecified() {
    return baseTreeId != null || baseTreeIdStr != null || baseCommitId != null || baseCommitIdStr != null;
  }

  protected void resolveBaseTree(@Nonnull Repository repository) throws IOException {
    if(baseTreeId == null) {
      if(baseTreeIdStr != null)
        baseTreeId = repository.resolve(baseTreeIdStr);
      else {
        if(baseCommitId == null)
          baseCommitId = repository.resolve(baseCommitIdStr);
        baseTreeId = CommitUtils.getCommit(baseCommitId, repository).getTree();
      }
    }
  }

  private void setupBase(@Nonnull CacheStateProvider provider) throws IOException {
    if(isBaseSpecified()) {
      resolveBaseTree(provider.getRepository());
      CacheUtils.addTree(provider.getCurrentCache(), provider.getReader(), "", baseTreeId);
    }
  }

  @Nonnull
  protected DirCache buildCache() throws IOException {
    try(CacheStateProvider provider = new CacheStateProvider(repository)) {
      setupBase(provider);
      for(CacheEditor editor : editors)
        editor.edit(provider);
      return provider.getCurrentCache();
    }
  }

}
