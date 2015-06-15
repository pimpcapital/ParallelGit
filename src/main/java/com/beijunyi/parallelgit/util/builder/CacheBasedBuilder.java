package com.beijunyi.parallelgit.util.builder;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.util.DirCacheHelper;
import com.beijunyi.parallelgit.util.RevTreeHelper;
import org.eclipse.jgit.dircache.*;
import org.eclipse.jgit.lib.*;

public abstract class CacheBasedBuilder<B extends CacheBasedBuilder, T> extends ParallelBuilder<T> {

  protected AnyObjectId baseCommitId;
  protected String baseCommitIdStr;
  protected AnyObjectId baseTreeId;
  protected String baseTreeIdStr;
  protected final List<CacheEditor> editors = new ArrayList<>();
  protected final Repository repository;

  protected CacheBasedBuilder(@Nullable Repository repository) {
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
  public B addTree(@Nonnull AnyObjectId treeId, @Nonnull String path) {
    AddTree editor = new AddTree(path);
    editor.setTreeId(treeId);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B addTree(@Nonnull String treeIdStr, @Nonnull String path) {
    AddTree editor = new AddTree(path);
    editor.setTreeIdStr(treeIdStr);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B addBlob(@Nonnull AnyObjectId blobId, @Nonnull FileMode mode, @Nonnull String path) {
    AddBlob editor = new AddBlob(path);
    editor.setBlobId(blobId);
    editor.setMode(mode);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B addBlob(@Nonnull AnyObjectId blobId, @Nonnull String path) {
    return addBlob(blobId, FileMode.REGULAR_FILE, path);
  }

  @Nonnull
  public B deleteTree(@Nonnull String path) {
    DeleteTree editor = new DeleteTree(path);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B deleteBlob(@Nonnull String path) {
    DeleteBlob editor = new DeleteBlob(path);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B updateBlob(@Nonnull AnyObjectId blobId, @Nonnull FileMode mode, @Nonnull String path) {
    UpdateBlob editor = new UpdateBlob(path);
    editor.setBlobId(blobId);
    editor.setMode(mode);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B updateBlob(@Nonnull AnyObjectId blobId, @Nonnull String path) {
    UpdateBlob editor = new UpdateBlob(path);
    editor.setBlobId(blobId);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B updateBlob(@Nonnull FileMode mode, @Nonnull String path) {
    UpdateBlob editor = new UpdateBlob(path);
    editor.setMode(mode);
    editors.add(editor);
    return self();
  }

  @Nonnull
  private Repository ensureRepository() {
    if(repository == null)
      throw new IllegalArgumentException("repository must be configured");
    return repository;
  }

  private void setupBase(@Nonnull DirCache cache) throws IOException {
    if(baseTreeId != null || baseTreeIdStr != null || baseCommitId != null || baseCommitIdStr != null) {
      Repository repository = ensureRepository();
      ObjectReader reader = repository.newObjectReader();
      try {
        if(baseTreeId == null) {
          if(baseTreeIdStr != null)
            baseTreeId = repository.resolve(baseTreeIdStr);
          else {
            if(baseCommitId == null)
              baseCommitId = repository.resolve(baseCommitIdStr);
            baseTreeId = RevTreeHelper.getRootTree(reader, baseCommitId);
          }
        }
        DirCacheHelper.addTree(cache, reader, "", baseTreeId);
      } finally {
        reader.release();
      }
    }
  }

  @Nonnull
  private DirCache setupCache() throws IOException {
    DirCache cache = DirCache.newInCore();
    setupBase(cache);
    return cache;
  }

  @Nonnull
  protected DirCache buildCache() throws IOException {
    DirCache cache = setupCache();
    try(BuildStateProvider provider = new BuildStateProvider(cache)) {
      for(CacheEditor editor : editors) {
        editor.doEdit(provider);
      }
      return provider.getCurrentCache();
    }
  }

  private class BuildStateProvider implements Closeable {
    private final DirCache cache;
    private ObjectReader reader;
    private DirCacheBuilder builder;
    private DirCacheEditor editor;

    private BuildStateProvider(@Nonnull DirCache cache) {
      this.cache = cache;
    }

    @Nonnull
    public DirCache getCurrentCache() {
      if(builder != null) {
        builder.finish();
        builder = null;
      }
      if(editor != null) {
        editor.finish();
        editor = null;
      }
      return cache;
    }

    @Nonnull
    public ObjectReader getCurrentReader() {
      if(reader == null)
        reader = ensureRepository().newObjectReader();
      return reader;
    }

    @Nonnull
    public DirCacheBuilder getCurrentBuilder() {
      if(editor != null) {
        editor.finish();
        editor = null;
      }
      if(builder == null)
        builder = DirCacheHelper.keepEverything(cache);
      return builder;
    }

    @Nonnull
    public DirCacheEditor getEditor() {
      if(builder != null) {
        builder.finish();
        builder = null;
      }
      if(editor == null)
        editor = cache.editor();
      return editor;
    }

    @Override
    public void close() {
      if(reader != null)
        reader.release();
    }
  }

  private abstract class CacheEditor {
    protected final String path;

    protected CacheEditor(@Nonnull String path) {
      this.path = path;
    }

    protected abstract void doEdit(@Nonnull BuildStateProvider provider) throws IOException;
  }

  private class AddTree extends CacheEditor {
    private AnyObjectId treeId;
    private String treeIdStr;

    private AddTree(@Nonnull String path) {
      super(path);
    }

    private void setTreeId(@Nullable AnyObjectId treeId) {
      this.treeId = treeId;
    }

    private void setTreeIdStr(@Nullable String treeIdStr) {
      this.treeIdStr = treeIdStr;
    }

    @Override
    protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
      ObjectReader reader = provider.getCurrentReader();
      if(treeId == null)
        treeId = ensureRepository().resolve(treeIdStr);
      DirCacheBuilder builder = provider.getCurrentBuilder();
      DirCacheHelper.addTree(builder, reader, path, treeId);
    }
  }

  private class AddBlob extends CacheEditor {
    private AnyObjectId blobId;
    private FileMode mode;

    private AddBlob(@Nonnull String path) {
      super(path);
    }

    public void setBlobId(@Nullable AnyObjectId blobId) {
      this.blobId = blobId;
    }

    public void setMode(@Nonnull FileMode mode) {
      this.mode = mode;
    }

    @Override
    protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
      DirCacheBuilder builder = provider.getCurrentBuilder();
      if(blobId == null)
        throw new IllegalArgumentException("blobId must be configured");
      if(mode == null)
        throw new IllegalArgumentException("mode must be configured");
      DirCacheHelper.addFile(builder, mode, path, blobId);
    }
  }

  private class DeleteTree extends CacheEditor {
    private DeleteTree(@Nonnull String path) {
      super(path);
    }

    @Override
    protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
      DirCacheHelper.deleteDirectory(provider.getEditor(), path);
    }
  }

  private class DeleteBlob extends CacheEditor {
    private DeleteBlob(@Nonnull String path) {
      super(path);
    }

    @Override
    protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
      DirCacheHelper.deleteFile(provider.getEditor(), path);
    }
  }

  private class UpdateBlob extends CacheEditor {
    private AnyObjectId blobId;
    private FileMode mode;

    private UpdateBlob(@Nonnull String path) {
      super(path);
    }

    public void setBlobId(@Nonnull AnyObjectId blobId) {
      this.blobId = blobId;
    }

    public void setMode(@Nonnull FileMode mode) {
      this.mode = mode;
    }

    @Override
    protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
      if(blobId == null && mode == null)
        throw new IllegalArgumentException("either of blobId or mode must be configured");
      DirCache cache = provider.getCurrentCache();
      DirCacheEntry entry = cache.getEntry(path);
      if(entry == null)
        throw new IllegalArgumentException("blob not found at " + path);
      if(blobId != null)
        entry.setObjectId(blobId);
      if(mode != null)
        entry.setFileMode(mode);
    }
  }

}
