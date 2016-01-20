package com.beijunyi.parallelgit.filesystem.merge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.filesystem.GfsObjectService;
import com.beijunyi.parallelgit.filesystem.GfsStatusProvider;
import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.exceptions.MergeNotStartedException;
import com.beijunyi.parallelgit.filesystem.io.GfsTreeIterator;
import com.beijunyi.parallelgit.utils.io.BlobSnapshot;
import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import static java.util.Arrays.asList;
import static org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm.HISTOGRAM;
import static org.eclipse.jgit.lib.ConfigConstants.*;
import static org.eclipse.jgit.lib.Constants.*;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;

public class GfsMerge extends ThreeWayMerger {

  private final GfsMergeChanges changes = new GfsMergeChanges();
  private final GitFileSystem gfs;
  private final GfsStatusProvider status;
  private final GfsObjectService objectService;

  private boolean formatConflicts = true;
  private MergeAlgorithm algorithm = defaultAlgorithm(db);
  private MergeFormatter formatter = defaultFormatter();
  private List<String> conflictMarkers = defaultConflictMarkers();

  private GfsMergeResult result;

  public GfsMerge(@Nonnull GitFileSystem gfs) {
    super(gfs.getRepository());
    this.gfs = gfs;
    this.status = gfs.getStatusProvider();
    this.objectService = gfs.getObjectService();
  }

  @Override
  protected boolean mergeImpl() throws IOException {
    TreeWalk tw = prepareTreeWalk();
    mergeTreeWalk(tw);
    return !changes.hasFailedPaths() && !changes.hasConflicts();
  }

  @Override
  public ObjectId getResultTreeId() {
    if(result == null)
      throw new MergeNotStartedException();
    return (ObjectId) result.getTree();
  }

  @Nonnull
  private TreeWalk prepareTreeWalk() throws IOException {
    TreeWalk ret = new NameConflictTreeWalk(gfs.getRepository());
    ret.addTree(mergeBase());
    ret.addTree(new CanonicalTreeParser(null, reader, sourceTrees[0]));
    ret.addTree(sourceTrees[1]);
    ret.addTree(new GfsTreeIterator(gfs));
    return ret;
  }

  private void mergeTreeWalk(@Nonnull TreeWalk tw) throws IOException {
    while(tw.next())
      if(mergeEntry(QuadWayEntry.read(tw)))
        tw.enterSubtree();
  }

  private boolean mergeEntry(@Nonnull QuadWayEntry entry) throws IOException {
    if(entry.target().equals(entry.head()) || entry.target().equals(entry.base()))
      return false;
    if(!entry.isDirty())
      return cleanMerge(entry);
    return dirtyMerge(entry);
  }

  private boolean cleanMerge(@Nonnull QuadWayEntry entry) throws IOException {
    if(entry.base().equals(entry.head())) {
      changes.addChange(entry.path(), entry.target());
      return false;
    }
    if(entry.head().isDirectory() || entry.target().isDirectory())
      return true;
    if(!entry.head().isDirectory() && !entry.target().isDirectory()) {
      if(entry.head().isGitLink() || entry.target().isGitLink())
        changes.addFailedPath(entry.path());
      else
        mergeFile(entry);
      return false;
    }
    changes.addConflict(entry);
    return false;
  }

  private boolean dirtyMerge(@Nonnull QuadWayEntry entry) {
    if(entry.base().equals(entry.head())) {
      if(!entry.target().equals(entry.worktree())) {
        if(entry.target().isDirectory() && entry.worktree().isDirectory())
          return true;
        changes.addFailedPath(entry.path());
      }
      return false;
    }
    throw new UnsupportedOperationException();
  }

  private void mergeFile(@Nonnull QuadWayEntry entry) throws IOException {
    FileMode mode = mergeFileModes(entry.base().getMode(), entry.head().getMode(), entry.target().getMode());
    if(entry.head().getId().equals(entry.target().getId())) {
      if(mode == null)
        changes.addFailedPath(entry.path());
      else if(!mode.equals(entry.head().getMode()))
        changes.addChange(entry.path(), new GitFileEntry(entry.head().getId(), mode));
    } else {
      changes.addConflict(entry);
      if(formatConflicts) {
        MergeResult<RawText> result = mergeContent(entry);
        AnyObjectId id = writeResult(result);
        changes.addChange(entry.path(), new GitFileEntry(id, mode != null ? mode : REGULAR_FILE));
      }
    }
  }

  @Nonnull
  private MergeResult<RawText> mergeContent(@Nonnull QuadWayEntry entry) throws IOException {
    RawText base = getRawText(entry.base().getId());
    RawText head = getRawText(entry.head().getId());
    RawText target = getRawText(entry.target().getId());
    return algorithm.merge(RawTextComparator.DEFAULT, base, head, target);
  }

  @Nonnull
  private RawText getRawText(@Nonnull AnyObjectId id) throws IOException {
    if(id.equals(ObjectId.zeroId()))
      return new RawText(new byte[0]);
    return new RawText(reader.open(id, OBJ_BLOB).getCachedBytes());
  }

  @Nonnull
  private AnyObjectId writeResult(@Nonnull MergeResult<RawText> result) throws IOException {
    byte[] bytes;
    try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      formatter.formatMerge(out, result, conflictMarkers, CHARACTER_ENCODING);
      bytes = out.toByteArray();
    }
    return objectService.write(BlobSnapshot.capture(bytes));
  }


  @Nonnull
  private static MergeAlgorithm defaultAlgorithm(@Nonnull Repository repo) {
    DiffAlgorithm.SupportedAlgorithm diffAlg = repo.getConfig().getEnum(CONFIG_DIFF_SECTION, null, CONFIG_KEY_ALGORITHM, HISTOGRAM);
    return new MergeAlgorithm(DiffAlgorithm.getAlgorithm(diffAlg));
  }

  @Nonnull
  private static MergeFormatter defaultFormatter() {
    return new MergeFormatter();
  }

  @Nonnull
  private static List<String> defaultConflictMarkers() {
    return asList("BASE", "OURS", "THEIRS");
  }

  @Nullable
  private static FileMode mergeFileModes(@Nonnull FileMode base, @Nonnull FileMode head, @Nonnull FileMode target) {
    if(head.equals(target))
      return head;
    if(base.equals(head))
      return target;
    if(base.equals(target))
      return head;
    return null;
  }

}
