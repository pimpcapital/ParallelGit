package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsFileStore;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;

import static java.util.Collections.unmodifiableList;
import static org.eclipse.jgit.lib.Constants.OBJECT_ID_LENGTH;

public class GfsTreeIterator extends WorkingTreeIterator {

  private final List<Map.Entry<String, GitFileEntry>> files;
  private int index = -1;
  private AnyObjectId id;

  public GfsTreeIterator(@Nonnull TreeSnapshot snapshot, @Nonnull GfsTreeIterator parent) {
    super(parent);
    files = toList(snapshot);
    next(1);
  }

  public GfsTreeIterator(@Nonnull TreeSnapshot snapshot) {
    super((WorkingTreeOptions) null);
    files = toList(snapshot);
    next(1);
  }

  public GfsTreeIterator(@Nonnull GfsFileStore store) throws IOException {
    this(getSnapshot(store));
  }

  public GfsTreeIterator(@Nonnull GitFileSystem gfs) throws IOException {
    this(gfs.getFileStore());
  }

  @Override
  public boolean isModified(@Nullable DirCacheEntry entry, boolean forceContentCheck, @Nonnull ObjectReader reader) throws IOException {
    GitFileEntry current = currentEntry().getValue();
    if(entry == null)
      return !current.isMissing();
    return !current.getId().equals(entry.getObjectId()) || !current.getMode().equals(entry.getFileMode());
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
    Map.Entry<String, GitFileEntry> tree = currentEntry();
    TreeSnapshot snapshot = TreeSnapshot.load(tree.getValue().getId(), reader);
    return new GfsTreeIterator(snapshot, this);
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
  private Map.Entry<String, GitFileEntry> currentEntry() {
    return files.get(index);
  }

  private void readEntry() {
    Map.Entry<String, GitFileEntry> entry = currentEntry();

    mode = entry.getValue().getMode().getBits();
    id = entry.getValue().getId();

    byte[] name = Constants.encode(entry.getKey());
    ensurePathCapacity(pathOffset + name.length, pathOffset);
    System.arraycopy(name, 0, path, pathOffset, name.length);
    pathLen = pathOffset + name.length;
  }

  @Nonnull
  private static List<Map.Entry<String, GitFileEntry>> toList(@Nonnull TreeSnapshot snapshot) {
    SortedMap<String, GitFileEntry> children = snapshot.getChildren();
    List<Map.Entry<String, GitFileEntry>> ret = new ArrayList<>(children.size());
    for(Map.Entry<String, GitFileEntry> entry : children.entrySet())
      ret.add(entry);
    return unmodifiableList(ret);
  }

  @Nonnull
  private static TreeSnapshot getSnapshot(@Nonnull GfsFileStore store) throws IOException {
    DirectoryNode root = store.getRoot();
    TreeSnapshot snapshot = root.loadSnapshotIfNotInitilized();
    if(snapshot == null) {
      snapshot = root.takeSnapshot(true, true);
      root.getObjService().flush();
    }
    assert snapshot != null;
    return snapshot;
  }

}
