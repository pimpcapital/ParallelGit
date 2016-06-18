package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsFileStore;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;

import static java.lang.System.arraycopy;
import static java.util.Collections.*;
import static org.eclipse.jgit.lib.Constants.*;

public class GfsTreeIterator extends WorkingTreeIterator {

  private final List<GfsTreeEntry> files;
  private int index = -1;
  private ObjectId id;

  private GfsTreeIterator(List<GfsTreeEntry> files, GfsTreeIterator parent) {
    super(parent);
    this.files = files;
    next(1);
  }

  private GfsTreeIterator(List<GfsTreeEntry> files) {
    super((WorkingTreeOptions) null);
    this.files = files;
    next(1);
  }

  private GfsTreeIterator(DirectoryNode node) throws IOException {
    this(GfsTreeEntry.listChildren(node));
  }

  private GfsTreeIterator(GfsFileStore store) throws IOException {
    this(store.getRoot());
  }

  private GfsTreeIterator(GitFileSystem gfs) throws IOException {
    this(gfs.getFileStore());
  }

  @Nonnull
  public static GfsTreeIterator iterateRoot(GitFileSystem gfs) throws IOException {
    return new GfsTreeIterator(gfs);
  }

  @Override
  public boolean isModified(@Nullable DirCacheEntry entry, boolean forceContentCheck, ObjectReader reader) throws IOException {
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
  public AbstractTreeIterator createSubtreeIterator(ObjectReader reader) throws IOException {
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
    if(!eof()) readEntry();
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

    byte[] name = encode(entry.getName());
    ensurePathCapacity(pathOffset + name.length, pathOffset);
    arraycopy(name, 0, path, pathOffset, name.length);
    pathLen = pathOffset + name.length;
  }

  private static class GfsTreeEntry {
    private final String name;
    private final Node node;

    private GfsTreeEntry(String name, Node node) {
      this.name = name;
      this.node = node;
    }

    @Nonnull
    public static GfsTreeEntry forNode(String name, Node node) {
      return new GfsTreeEntry(name, node);
    }

    @Nonnull
    public String getName() {
      return name;
    }

    @Nonnull
    public ObjectId getId() {
      try {
        return node.getObjectId(false);
      } catch(IOException e) {
        throw new IllegalStateException(e);
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
    public static List<GfsTreeEntry> listChildren(DirectoryNode dir) throws IOException {
      List<GfsTreeEntry> ret = new ArrayList<>();
      for(Map.Entry<String, Node> child : dir.getData().entrySet()) {
        Node node = child.getValue();
        if(!node.isTrivial()) ret.add(forNode(child.getKey(), node));
      }
      sort(ret, TreeEntryComparator.ASCENDING);
      return unmodifiableList(ret);
    }
  }

  private static class TreeEntryComparator implements Comparator<GfsTreeEntry> {

    private static TreeEntryComparator ASCENDING = new TreeEntryComparator();

    @Override
    public int compare(GfsTreeEntry o1, GfsTreeEntry o2) {
      return o1.getName().compareTo(o2.getName());
    }

  }

}
