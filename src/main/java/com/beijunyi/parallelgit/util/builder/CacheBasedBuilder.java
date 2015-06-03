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

  private final List<CacheEditor> editors = new ArrayList<>();

  protected CacheBasedBuilder(@Nonnull Repository repository) {
    super(repository);
  }

  @Nonnull
  protected abstract B self();

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
  public B loadRevision(@Nonnull AnyObjectId revisionId) {
    AddTree editor = new AddTree("");
    editor.setRevisionId(revisionId);
    editors.add(editor);
    return self();
  }

  @Nonnull
  public B loadRevision(@Nonnull String revisionIdStr) {
    AddTree editor = new AddTree("");
    editor.setRevisionIdStr(revisionIdStr);
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
  protected DirCache buildCache(@Nonnull Repository repository) throws IOException {
    DirCache cache = DirCache.newInCore();
    try(BuildStateProvider provider = new BuildStateProvider(repository, cache)) {
      for(CacheEditor editor : editors) {
        editor.doEdit(provider);
      }
    }
    return cache;
  }

  private class BuildStateProvider implements Closeable {
    private final Repository repository;
    private final DirCache cache;
    private ObjectReader reader;
    private DirCacheBuilder builder;
    private DirCacheEditor editor;

    private BuildStateProvider(@Nonnull Repository repository, @Nonnull DirCache cache) {
      this.repository = repository;
      this.cache = cache;
    }

    @Nonnull
    public Repository getRepository() {
      return repository;
    }

    @Nonnull
    public DirCache getCache() {
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
    public ObjectReader getReader() {
      if(reader == null)
        reader = repository.newObjectReader();
      return reader;
    }

    @Nonnull
    public DirCacheBuilder getBuilder() {
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
      if(builder != null)
        builder.finish();
      if(editor != null)
        editor.finish();
    }
  }

  private abstract class CacheEditor {
    protected final String path;

    protected CacheEditor(String path) {
      this.path = path;
    }

    protected abstract void doEdit(@Nonnull BuildStateProvider provider) throws IOException;
  }

  private class AddTree extends CacheEditor {
    private AnyObjectId treeId;
    private String treeIdStr;
    private AnyObjectId revisionId;
    private String revisionIdStr;

    private AddTree(@Nonnull String path) {
      super(path);
    }

    private void setTreeId(@Nullable AnyObjectId treeId) {
      this.treeId = treeId;
    }

    private void setTreeIdStr(@Nullable String treeIdStr) {
      this.treeIdStr = treeIdStr;
    }

    private void setRevisionId(@Nullable AnyObjectId revisionId) {
      this.revisionId = revisionId;
    }

    private void setRevisionIdStr(@Nullable String revisionIdStr) {
      this.revisionIdStr = revisionIdStr;
    }

    @Override
    protected void doEdit(@Nonnull BuildStateProvider provider) throws IOException {
      ObjectReader reader = provider.getReader();
      DirCacheBuilder builder = provider.getBuilder();
      if(path == null)
        throw new IllegalArgumentException("path must be configured");
      if(treeId == null) {
        if(treeIdStr != null)
          treeId = repository.resolve(treeIdStr);
        else if(revisionId != null)
          treeId = RevTreeHelper.getRootTree(reader, revisionId);
        else if(revisionIdStr != null)
          treeId = RevTreeHelper.getRootTree(reader, repository.resolve(revisionIdStr));
        else
          throw new IllegalArgumentException("either of treeId of revisionId must be configured");
      }
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
      DirCacheBuilder builder = provider.getBuilder();
      if(blobId == null)
        throw new IllegalArgumentException("blobId must be configured");
      if(path == null)
        throw new IllegalArgumentException("path must be configured");
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
      DirCache cache = provider.getCache();
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
