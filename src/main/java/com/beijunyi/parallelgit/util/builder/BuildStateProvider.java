package com.beijunyi.parallelgit.util.builder;

import java.io.Closeable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.util.DirCacheHelper;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

class BuildStateProvider implements Closeable {
  private final DirCache cache = DirCache.newInCore();
  private final Repository repository;
  private ObjectReader reader;
  private ObjectInserter inserter;
  private DirCacheBuilder builder;
  private DirCacheEditor editor;

  BuildStateProvider(@Nullable Repository repository) {
    this.repository = repository;
  }

  @Nonnull
  DirCache getCurrentCache() {
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
  Repository getRepository() {
    if(repository == null)
      throw new IllegalArgumentException("Repository is not configured");
    return repository;
  }

  @Nonnull
  ObjectReader getReader() {
    if(reader == null)
      reader = getRepository().newObjectReader();
    return reader;
  }

  @Nonnull
  ObjectInserter getInserter() {
    if(inserter == null)
      inserter = getRepository().newObjectInserter();
    return inserter;
  }

  @Nonnull
  DirCacheBuilder getCurrentBuilder() {
    if(editor != null) {
      editor.finish();
      editor = null;
    }
    if(builder == null)
      builder = DirCacheHelper.keepEverything(cache);
    return builder;
  }

  @Nonnull
  DirCacheEditor getEditor() {
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
    if(inserter != null)
      inserter.release();
  }
}