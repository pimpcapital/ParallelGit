package com.beijunyi.parallelgit.web.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.UnsuccessfulOperationException;
import com.beijunyi.parallelgit.web.data.FileType;
import com.beijunyi.parallelgit.web.protocol.model.DeltaType;
import com.beijunyi.parallelgit.web.protocol.model.FileDelta;
import com.beijunyi.parallelgit.web.protocol.model.FileState;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.*;

import static com.beijunyi.parallelgit.utils.CommitUtils.getCommit;
import static com.beijunyi.parallelgit.utils.TreeUtils.getObjectId;
import static com.beijunyi.parallelgit.web.data.FileType.MISSING;
import static com.beijunyi.parallelgit.web.protocol.model.DeltaType.*;
import static org.eclipse.jgit.lib.Constants.HEAD;
import static org.eclipse.jgit.treewalk.filter.TreeFilter.ANY_DIFF;

public class DiffDirectoryHandler extends AbstractGfsRequestHandler {

  private static final int CURRENT = 0;
  private static final int PREVIOUS = 1;

  @Nonnull
  @Override
  public String getType() {
    return "diff-directory";
  }

  @Nonnull
  @Override
  public ServerResponse handle(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    AbstractTreeIterator source = getSourceTree(request, gfs);
    AbstractTreeIterator target = getTargetTree(request, gfs);
    TreeWalk tw = setupDiffTree(source, target, gfs.getRepository());
    List<FileDelta> deltas = collectDeltas(tw);
    return request.respond().ok(deltas);
  }

  @Nonnull
  private static AbstractTreeIterator getSourceTree(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    String revision = request.getString("source-revision");
    String path = request.getString("source-path");
    return getTree(revision, path, gfs);
  }

  @Nonnull
  private static AbstractTreeIterator getTargetTree(@Nonnull ClientRequest request, @Nonnull GitFileSystem gfs) throws IOException {
    String revision = request.getString("target-revision");
    String path = request.getString("target-path");
    return getTree(revision, path, gfs);
  }

  @Nonnull
  private static AbstractTreeIterator getTree(@Nonnull String revision, @Nonnull String path, @Nonnull GitFileSystem gfs) throws IOException {
    AbstractTreeIterator ret;
    if(HEAD.equals(revision)) {
      Repository repo = gfs.getRepository();
      ret = getTreeFromRevision(revision, path, repo);
    } else {
      ret = getTreeFromGfs(path, gfs);
    }
    return ret;
  }

  @Nonnull
  private static AbstractTreeIterator getTreeFromRevision(@Nonnull String revision, @Nonnull String path, @Nonnull Repository repo) throws IOException{
    AnyObjectId rootTree = getCommit(revision, repo).getTree();
    AnyObjectId dirTree = getObjectId(path, rootTree, repo);
    if(dirTree != null) {
      CanonicalTreeParser treeParser = new CanonicalTreeParser();
      treeParser.reset(repo.newObjectReader(), dirTree);
      return treeParser;
    } else {
      return new EmptyTreeIterator();
    }
  }

  @Nonnull
  private static AbstractTreeIterator getTreeFromGfs(@Nonnull String path, @Nonnull GitFileSystem gfs) throws IOException{
    throw new UnsuccessfulOperationException();
  }

  @Nonnull
  private static TreeWalk setupDiffTree(@Nonnull AbstractTreeIterator source, @Nonnull AbstractTreeIterator target, @Nonnull Repository repo) throws IOException {
    TreeWalk ret = new TreeWalk(repo);
    ret.addTree(source);
    ret.addTree(target);
    ret.setRecursive(true);
    ret.setFilter(ANY_DIFF);
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
        return DATA_AND_TYPE_DELTA;
      if(contentChanged)
        return DATA_DELTA;
      if(typeChanged)
        return TYPE_DELTA;
    }
    if(previous.getType() == MISSING && current.getType() != MISSING)
      return INSERTION;
    if(previous.getType() != MISSING && current.getType() == MISSING)
      return DELETION;
    throw new IllegalStateException();
  }

}
