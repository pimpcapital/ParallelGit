package com.beijunyi.parallelgit.filesystem.merge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import com.beijunyi.parallelgit.filesystem.GitFileSystem;
import com.beijunyi.parallelgit.filesystem.io.GfsCheckout;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.merge.MergeFormatter;
import org.eclipse.jgit.merge.MergeResult;

import static com.beijunyi.parallelgit.filesystem.utils.GfsPathUtils.toAbsolutePath;
import static org.eclipse.jgit.lib.Constants.CHARACTER_ENCODING;
import static org.eclipse.jgit.lib.FileMode.REGULAR_FILE;

public class GfsMergeCheckout extends GfsCheckout {

  private String base = "BASE";
  private String ours = "OURS";
  private String theirs = "THEIRS";
  private Map<String, MergeResult<? extends Sequence>> conflicts;
  private MergeFormatter formatter;

  public GfsMergeCheckout(@Nonnull GitFileSystem gfs) {
    super(gfs);
  }

  @Nonnull
  public GfsMergeCheckout base(@Nonnull String base) {
    this.base = base;
    return this;
  }

  @Nonnull
  public GfsMergeCheckout ours(@Nonnull String ours) {
    this.ours = ours;
    return this;
  }

  @Nonnull
  public GfsMergeCheckout theirs(@Nonnull String theirs) {
    this.theirs = theirs;
    return this;
  }

  @Nonnull
  public GfsMergeCheckout handleConflicts(@Nonnull Map<String, MergeResult<? extends Sequence>> conflicts) {
    this.conflicts = conflicts;
    return this;
  }

  @Nonnull
  public GfsMergeCheckout withFormatter(@Nonnull MergeFormatter formatter) {
    this.formatter = formatter;
    return this;
  }

  @Override
  protected boolean skips(@Nonnull String path) {
    return super.skips(path) || (conflicts != null && conflicts.containsKey(path));
  }

  @Override
  protected void applyChanges() throws IOException {
    addFormattedConflicts();
    super.applyChanges();
  }

  private void addFormattedConflicts() throws IOException {
    if(conflicts != null) {
      for(Map.Entry<String, MergeResult<? extends Sequence>> conflict : conflicts.entrySet()) {
        String path = conflict.getKey();
        byte[] formatted = formatConflict(conflict.getValue());
        changes.addChange(path, formatted, REGULAR_FILE);
      }
    }
  }

  @Nonnull
  private byte[] formatConflict(@Nonnull MergeResult<? extends Sequence> conflict) throws IOException {
    try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
      formatter.formatMerge(stream, conflict, base, theirs, ours, CHARACTER_ENCODING);
      return stream.toByteArray();
    }
  }

}
