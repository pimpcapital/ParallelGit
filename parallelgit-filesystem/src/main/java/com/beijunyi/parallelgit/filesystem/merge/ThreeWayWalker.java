package com.beijunyi.parallelgit.filesystem.merge;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.io.DirectoryNode;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.TreeWalk;

public class ThreeWayWalker implements Iterator<ThreeWayEntry>, AutoCloseable {

  private final TreeWalk tw;
  private final LinkedList<DirectoryNode> dirs;
  private IOException error;

  private ThreeWayEntry next;
  private ThreeWayEntry current;

  public ThreeWayWalker(@Nonnull ThreeWayWalkerConfig config, @Nonnull ObjectReader reader) throws IOException {
    tw = config.makeTreeWalk(reader);
    dirs = config.getDirectories();
  }

  @Override
  public boolean hasNext() {
    findNextEntry();
    return error == null && next != null;
  }

  @Nonnull
  @Override
  public ThreeWayEntry next() {
    findNextEntry();
    if(next == null)
      throw new NoSuchElementException();
    prepareCurrentEntry();
    return current;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    tw.close();
  }

  public void enterDirectory() throws IOException {
    if(current == null)
      throw new IllegalStateException();
    tw.enterSubtree();
  }

  @Nullable
  public IOException getError() {
    return error;
  }

  private void findNextEntry() {
    if(next != null)
      return;
    try {
      if(tw.next())
        next = ThreeWayEntry.read(tw);
    } catch(IOException e) {
      error = e;
    }
  }

  private void prepareCurrentEntry() {
    current = next;
    next = null;
    int prevDepth = dirs.size();
    for(int d = prevDepth; d > current.getDepth(); d--)
      dirs.removeLast();
  }

}
