package com.beijunyi.parallelgit.filesystem.merge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.beijunyi.parallelgit.utils.io.GitFileEntry;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.MergeResult;

import static org.eclipse.jgit.lib.Constants.*;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;

public class GfsRecursiveMerger {

  private final ThreeWayWalker walker;
  private final ContentMergeConfig config;
  private final ObjectReader reader;

  private final Map<String, GfsMergeConflict> conflicts = new HashMap<>();

  private ThreeWayEntry next;
  private GitFileEntry base;
  private GitFileEntry ours;
  private GitFileEntry theirs;

  public GfsRecursiveMerger(@Nonnull ThreeWayWalker walker, @Nonnull ContentMergeConfig config, @Nonnull ObjectReader reader) {
    this.walker = walker;
    this.config = config;
    this.reader = reader;
  }

  @Nonnull
  public Map<String, GfsMergeConflict> getConflicts() {
    return conflicts;
  }

  public boolean merge() throws IOException {
    while(walker.hasNext()) {
      next = walker.next();
      readEntry();
      mergeEntry();
    }
    IOException error = walker.getError();
    if(error != null)
      throw error;
    return conflicts.isEmpty();
  }

  private void readEntry() {
    base = next.getBase();
    ours = next.getOurs();
    theirs = next.getTheirs();
  }

  private void mergeEntry() throws IOException {
    if(!applyObjectDiff() && !mergeObject()) {
      GfsMergeConflict conflict = new GfsMergeConflict(next);
      conflicts.put(next.getPath(), conflict);
    }
  }

  private boolean applyObjectDiff() {
    if(base.equals(ours)) {
      apply(theirs);
      return true;
    }
    if(base.equals(theirs)) {
      apply(ours);
      return true;
    }
    if(ours.hasSameObjectAs(theirs)) {
      if(ours.hasSameModeAs(theirs)) {
        apply(ours);
        return true;
      }
      FileMode mergedMode = mergeMode();
      if(mergedMode != null) {
        apply(new GitFileEntry(ours.getId(), mergedMode));
        return true;
      }
    }
    return false;
  }

  @Nullable
  private FileMode mergeMode() {
    if(ours.hasSameModeAs(theirs))
      return ours.getMode();
    if(base.hasSameModeAs(ours))
      return theirs.isMissing() ? ours.getMode() : theirs.getMode();
    if(base.hasSameModeAs(theirs))
      return ours.isMissing() ? theirs.getMode() : ours.getMode();
    return null;
  }

  private boolean mergeObject() throws IOException {
    if(ours.isGitLink() || theirs.isGitLink())
      return false;
    if(!ours.isDirectory() && !theirs.isDirectory()) {
      MergeResult<RawText> result = mergeContent();
      writeMergedFile(result);
      return result.containsConflicts();
    }
    if(ours.isDirectory() && theirs.isDirectory()) {
      enterDirectory();
      return true;
    }
    return false;
  }

  @Nonnull
  private MergeResult<RawText> mergeContent() throws IOException {
    RawText baseContent = getRawText(base.getId());
    RawText ourContent = getRawText(ours.getId());
    RawText theirContent = getRawText(theirs.getId());
    return config.getAlgorithm().merge(RawTextComparator.DEFAULT, baseContent, ourContent, theirContent);
  }

  @Nonnull
  private RawText getRawText(@Nonnull AnyObjectId id) throws IOException {
    if(id.equals(ObjectId.zeroId()))
      return new RawText(new byte[0]);
    return new RawText(reader.open(id, OBJ_BLOB).getCachedBytes());
  }

  private void writeMergedFile(@Nonnull MergeResult<RawText> result) throws IOException {
    FileMode mode = mergeMode();
    if(mode == null)
      mode = REGULAR_FILE;

    byte[] bytes;
    try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      config.getFormatter().formatMerge(out, result, config.getConflictMarkers(), CHARACTER_ENCODING);
      bytes = out.toByteArray();
    }

    apply(bytes, mode);
  }

  private void enterDirectory() {

  }

  private void apply(@Nonnull GitFileEntry entry) {

  }

  private void apply(@Nonnull byte[] bytes, @Nonnull FileMode mode) {

  }

}
