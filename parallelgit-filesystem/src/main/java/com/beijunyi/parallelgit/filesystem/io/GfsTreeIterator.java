package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsFileStore;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;

import static org.eclipse.jgit.lib.Constants.OBJECT_ID_LENGTH;

public class GfsTreeIterator extends WorkingTreeIterator {

  private final List<GfsTreeEntry> files;
  private int index = -1;
  private AnyObjectId id;

  private GfsTreeIterator(@Nonnull List<GfsTreeEntry> files, @Nonnull GfsTreeIterator parent) {
    super(parent);
    this.files = files;
    next(1);
  }

  private GfsTreeIterator(@Nonnull List<GfsTreeEntry> files) {
    super((WorkingTreeOptions) null);
    this.files = files;
    next(1);
  }

  private GfsTreeIterator(@Nonnull DirectoryNode node) throws IOException {
    this(GfsTreeEntry.listChildren(node));
  }

  private GfsTreeIterator(@Nonnull GfsFileStore store) throws IOException {
    this(store.getRoot());
  }

  public GfsTreeIterator(@Nonnull GitFileSystem gfs) throws IOException {
    this(gfs.getFileStore());
  }

  @Override
  public boolean isModified(@Nullable DirCacheEntry entry, boolean forceContentCheck, @Nonnull ObjectReader reader) throws IOException {
    GfsTreeEntry current = currentEntry();
    return entry == null || !current.getId().equals(entry.getObjectId()) || !current.getMode().equals(entry.getFileMode());
  }

  @Override
  public boolean hasId() {
    return index >= 0 && index < files.size();
  }

  @Override
  public byte[] idBuffer() {
    byte[] ret = new byte[OBJECT_ID_LENGTH];
    id.copyRawTo(ret, 0);
    return ret;
  }

  @Override
  public int idOffset() {
    return 0;
  }

  @Nonnull
  @Override
  public AbstractTreeIterator createSubtreeIterator(@Nonnull ObjectReader reader) throws IOException {
    GfsTreeEntry entry = currentEntry();
    return new GfsTreeIterator(entry.listChildren(), this);
  }

  @Override
  public boolean first() {
    return index == 0;
  }

  @Override
  public boolean eof() {
    return index == files.size();
  }

  @Override
  public void next(int delta) {
    index = Math.min(files.size(), index + delta);
    if(!eof())
      readEntry();
  }

  @Override
  public void back(int delta) {
    index = Math.max(0, index - delta);
    readEntry();
  }

  @Nonnull
  private GfsTreeEntry currentEntry() {
    return files.get(index);
  }

  private void readEntry() {
    GfsTreeEntry entry = currentEntry();

    mode = entry.getMode().getBits();
    id = entry.getId();

    byte[] name = Constants.encode(entry.getName());
    ensurePathCapacity(pathOffset + name.length, pathOffset);
    System.arraycopy(name, 0, path, pathOffset, name.length);
    pathLen = pathOffset + name.length;
  }

  private static class GfsTreeEntry implements Comparable<GfsTreeEntry> {
    private final String name;
    private final Node node;

    public GfsTreeEntry(@Nonnull String name, @Nonnull Node node) {
      this.name = name;
      this.node = node;
    }

    @Override
    public int compareTo(@Nonnull GfsTreeEntry that) {
      return getName().compareTo(that.getName());
    }

    @Nonnull
    public String getName() {
      return name;
    }

    @Nonnull
    public AnyObjectId getId() {
      try {
        return node.getObjectId(false);
      } catch(IOException e) {
        throw new IllegalStateException();
      }
    }

    @Nonnull
    public FileMode getMode() {
      return node.getMode();
    }

    @Nonnull
    public List<GfsTreeEntry> listChildren() throws IOException {
      if(!node.isDirectory())
        throw new IllegalStateException();
      return listChildren((DirectoryNode) node);
    }

    @Nonnull
    public static List<GfsTreeEntry> listChildren(@Nonnull DirectoryNode dir) throws IOException {
      List<GfsTreeEntry> ret = new ArrayList<>();
      for(Map.Entry<String, Node> child : dir.getData().entrySet()) {
        Node node = child.getValue();
        if(!node.isTrivial())
          ret.add(new GfsTreeEntry(child.getKey(), node));
      }
      Collections.sort(ret);
      return ret;
    }
  }

}
