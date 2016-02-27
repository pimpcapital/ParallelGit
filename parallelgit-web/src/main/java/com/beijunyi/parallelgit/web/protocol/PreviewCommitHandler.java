package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator;
import com.beijunyi.parallelgit.web.data.FileType;
import com.beijunyi.parallelgit.web.protocol.model.DeltaType;
import com.beijunyi.parallelgit.web.protocol.model.FileDelta;
import com.beijunyi.parallelgit.web.protocol.model.FileState;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import static com.beijunyi.parallelgit.web.data.FileType.MISSING;

public class PreviewCommitHandler extends AbstractGfsRequestHandler {

  private static final int CURRENT = 0;
  private static final int PREVIOUS = 1;

  @Nonnull
  @Override
  public String getType() {
    return "preview-commit";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    TreeWalk tw = setupDiffTreeWalk(gfs);
    List<FileDelta> deltas = collectDeltas(tw);
    return request.respond().ok(deltas);
  }

  @Nonnull
  private static TreeWalk setupDiffTreeWalk(@Nonnull GitFileSystem gfs) throws IOException {
    TreeWalk ret = new TreeWalk(gfs.getRepository());
    GfsStatusProvider status = gfs.getStatusProvider();
    ret.addTree(new GfsTreeIterator(gfs));
    if(status.isInitialized())
      ret.addTree(status.commit());
    else
      ret.addTree(new EmptyTreeIterator());
    ret.setRecursive(true);
    ret.setFilter(TreeFilter.ANY_DIFF);
    return ret;
  }

  @Nonnull
  private static List<FileDelta> collectDeltas(@Nonnull TreeWalk tw) throws IOException {
    List<FileDelta> ret = new ArrayList<>();
    while(tw.next()) {
      FileState current = readState(tw, CURRENT);
      FileState previous = readState(tw, PREVIOUS);
      DeltaType type = computeDeltaType(current, previous);
      ret.add(new FileDelta(tw.getPathString(), current, previous, type));
    }
    return ret;
  }

  @Nonnull
  private static FileState readState(@Nonnull TreeWalk tw, int index) {
    return new FileState(tw.getObjectId(index).getName(), FileType.fromMode(tw.getFileMode(index)));
  }


  @Nonnull
  private static DeltaType computeDeltaType(@Nonnull FileState current, @Nonnull FileState previous) {
    if(previous.getType() != MISSING && current.getType() != MISSING) {
      boolean contentChanged = previous.getHash().equals(current.getHash());
      boolean typeChanged = previous.getType().equals(current.getType());
      if(contentChanged && typeChanged)
        return DeltaType.DATA_AND_TYPE_DELTA;
      if(contentChanged)
        return DeltaType.DATA_DELTA;
      if(typeChanged)
        return DeltaType.TYPE_DELTA;
    }
    if(previous.getType() == MISSING && current.getType() != MISSING)
      return DeltaType.INSERTION;
    if(previous.getType() != MISSING && current.getType() == MISSING)
      return DeltaType.DELETION;
    throw new IllegalStateException();
  }

}
