package com.beijunyi.parallelgit.filesystem.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsFileStore;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import com.beijunyi.parallelgit.utils.io.TreeSnapshot;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;

import static java.util.Collections.unmodifiableList;
import static org.eclipse.jgit.lib.Constants.OBJECT_ID_LENGTH;

public class GfsTreeIterator extends AbstractTreeIterator {

  private final List<Entry<String, GitFileEntry>> entries;
  private int index = -1;
  private AnyObjectId id;

  public GfsTreeIterator(@Nonnull TreeSnapshot snapshot, @Nonnull GfsTreeIterator parent) {
    super(parent);
    entries = toList(snapshot);
    next(1);
  }

  public GfsTreeIterator(@Nonnull TreeSnapshot snapshot) {
    entries = toList(snapshot);
    next(1);
  }

  public GfsTreeIterator(@Nonnull GfsFileStore store) throws IOException {
    this(getSnapshot(store));
  }

  public GfsTreeIterator(@Nonnull GitFileSystem gfs) throws IOException {
    this(gfs.getFileStore());
  }

  @Override
  public boolean hasId() {
    return index >= 0 && index < entries.size();
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
    Entry<String, GitFileEntry> tree = entries.get(index);
    TreeSnapshot snapshot = TreeSnapshot.load(tree.getValue().getId(), reader);
    return new GfsTreeIterator(snapshot, this);
  }

  @Override
  public boolean first() {
    return index == 0;
  }

  @Override
  public boolean eof() {
    return index == entries.size();
  }

  @Override
  public void next(int delta) {
    index = Math.min(entries.size(), index + delta);
    if(!eof())
      readNode();
  }

  @Override
  public void back(int delta) {
    index = Math.max(0, index - delta);
    readNode();
  }

  private void readNode() {
    Entry<String, GitFileEntry> entry = entries.get(index);

    mode = entry.getValue().getMode().getBits();
    id = entry.getValue().getId();

    byte[] name = Constants.encode(entry.getKey());
    ensurePathCapacity(pathOffset + name.length, pathOffset);
    System.arraycopy(name, 0, path, pathOffset, name.length);
    pathLen = pathOffset + name.length;
  }

  @Nonnull
  private static List<Entry<String, GitFileEntry>> toList(@Nonnull TreeSnapshot snapshot) {
    SortedMap<String, GitFileEntry> children = snapshot.getChildren();
    List<Entry<String, GitFileEntry>> ret = new ArrayList<>(children.size());
    for(Entry<String, GitFileEntry> entry : children.entrySet())
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
