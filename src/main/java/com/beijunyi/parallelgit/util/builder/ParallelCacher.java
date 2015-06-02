package com.beijunyi.parallelgit.util.builder;

import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.util.DirCacheHelper;
import com.beijunyi.parallelgit.util.RevTreeHelper;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.lib.*;

public final class ParallelCacher extends ParallelBuilder<DirCache> {

  private ParallelCacher(@Nonnull Repository repository) {
    super(repository);
  }

  @Nonnull
  @Override
  protected DirCache doBuild() throws IOException {
    return null;
  }

  @Nonnull
  public static ParallelCacher prepare(@Nonnull Repository repository) {
    return new ParallelCacher(repository);
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
      repository.close();
    }
  }

  private abstract class CacheEditor {
    protected abstract void doEdit(@Nonnull BuildStateProvider provider) throws IOException;
  }

  private class AddTree extends CacheEditor {
    private AnyObjectId treeId;
    private String treeIdStr;
    private AnyObjectId revisionId;
    private String revisionIdStr;
    private String path;

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

    private void setPath(@Nullable String path) {
      this.path = path;
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
    private String path;

    public void setBlobId(@Nullable AnyObjectId blobId) {
      this.blobId = blobId;
    }

    public void setMode(@Nonnull FileMode mode) {
      this.mode = mode;
    }

    public void setPath(@Nonnull String path) {
      this.path = path;
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



}
