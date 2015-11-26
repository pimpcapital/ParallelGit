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

  public ThreeWayWalker(@Nonnull ThreeWayWalkerConfiguration config, @Nonnull ObjectReader reader) throws IOException {
    tw = config.prepareTreeWalk(reader);
    dirs = config.getDirectories();
  }

  @Override
  public boolean hasNext() {
    prepareNext();
    return error == null && next != null;
  }

  @Nonnull
  @Override
  public ThreeWayEntry next() {
    prepareNext();
    if(next == null)
      throw new NoSuchElementException();
    return next;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws Exception {
    tw.close();
  }

  @Nullable
  public IOException getError() {
    return error;
  }

  private void prepareNext() {
    if(next != null)
      return;
    try {
      if(tw.next())
        next = ThreeWayEntry.read(tw);
    } catch(IOException e) {
      error = e;
    }
  }

}
