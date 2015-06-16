package com.beijunyi.parallelgit.command.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.FileMode;

public class UpdateFile extends CacheEditor {

  private byte[] bytes;
  private String content;
  private InputStream inputStream;
  private Path sourcePath;
  private File sourceFile;
  private FileMode mode;
  private boolean create;

  public UpdateFile(@Nonnull String path) {
    super(path);
  }

  public boolean isCreate() {
    return create;
  }

  public void setCreate(boolean create) {
    this.create = create;
  }

  private void createEntry(@Nonnull CacheStateProvider provider) throws IOException {

  }


  private void updateEntry(@Nonnull DirCacheEntry entry, @Nonnull CacheStateProvider provider) throws IOException {

  }

  @Override
  public void edit(@Nonnull CacheStateProvider provider) throws IOException {
    DirCache cache = provider.getCurrentCache();
    DirCacheEntry entry = cache.getEntry(path);
    if(entry == null) {
      if(!create)
        throw new IllegalArgumentException("Entry not found: " + path);
      createEntry(provider);
    } else
      updateEntry(entry, provider);
  }

}
