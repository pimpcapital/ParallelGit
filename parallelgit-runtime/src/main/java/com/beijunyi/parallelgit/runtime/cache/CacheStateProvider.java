package com.beijunyi.parallelgit.runtime.cache;

import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.CacheHelper;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

public class CacheStateProvider implements Closeable {
  private final DirCache cache = DirCache.newInCore();
  private final Repository repository;
  private ObjectReader reader;
  private ObjectInserter inserter;
  private DirCacheBuilder builder;
  private DirCacheEditor editor;

  public CacheStateProvider(@Nullable Repository repository) {
    this.repository = repository;
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
  public Repository getRepository() {
    if(repository == null)
      throw new IllegalArgumentException("Repository is not configured");
    return repository;
  }

  @Nonnull
  public ObjectReader getReader() {
    if(reader == null)
      reader = getRepository().newObjectReader();
    return reader;
  }

  @Nonnull
  public ObjectInserter getInserter() {
    if(inserter == null)
      inserter = getRepository().newObjectInserter();
    return inserter;
  }

  @Nonnull
  public DirCacheBuilder getCurrentBuilder() {
    if(editor != null) {
      editor.finish();
      editor = null;
    }
    if(builder == null)
      builder = CacheHelper.keepEverything(cache);
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
  public void close() throws IOException {
    if(reader != null)
      reader.release();
    if(inserter != null) {
      inserter.flush();
      inserter.release();
    }
  }
}